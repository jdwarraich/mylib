package com.jd.mylib.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.media.MediaMetadataRetriever
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.text.Html
import android.text.InputFilter
import android.text.SpannableString
import android.text.Spanned
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.*
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.Group
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.jd.mylib.R
import com.jd.mylib.helper.Logs
import com.jd.mylib.helper.SETTINGS_REQUEST_CODE
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.math.ceil
import kotlin.math.log10
import kotlin.math.pow

private const val TAG = "Extensions"
fun View.show() {
    visibility = View.VISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun View.hide() {
    visibility = View.INVISIBLE
}

fun View.animateView(@AnimRes animationId: Int, duration: Long = 500L) {
    val animation = AnimationUtils.loadAnimation(context, animationId)
    animation.duration = duration
    animation.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationStart(animation: Animation?) = Unit
        override fun onAnimationRepeat(animation: Animation?) = Unit
        override fun onAnimationEnd(animation: Animation?) {
            if (animationId == R.anim.fade_out || animationId == R.anim.slide_out_up) this@animateView.gone()
            else if (animationId == R.anim.slide_in_down) this@animateView.show()
        }
    })
    startAnimation(animation)
}

fun View.hideWithAnimation(callback: () -> Unit) {
    try {
        val animation = AnimationUtils.loadAnimation(context, R.anim.fade_out)
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) = Unit
            override fun onAnimationRepeat(animation: Animation?) = Unit
            override fun onAnimationEnd(animation: Animation?) {
                visibility = View.INVISIBLE
                callback()
            }
        })
        requestLayout()
        startAnimation(animation)
    } catch (e: Exception) {
    }
}

fun TextView.tintViewDrawable(@ColorRes colorId: Int) {
    val drawables = compoundDrawables
    for (drawable in drawables) {
        drawable?.setColorFilter(
            ContextCompat.getColor(context, colorId),
            PorterDuff.Mode.SRC_ATOP
        )
    }
}

fun delay(time: Long = 500L, runnable: Runnable): Handler {
    val handler = Handler(Looper.getMainLooper())
    handler.postDelayed(runnable, time)
    return handler
}

fun View.hideKeyboard() {
    val inputMethodManager =
        context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
}

fun View.showKeyboard() {
    val inputMethodManager =
        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
    inputMethodManager?.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
}

fun EditText?.moveCursorToEnd() = this?.setSelection(this.text.length)

fun EditText.setFileRenameFilters() {
    val blockCharacterSet = "\\ / : * ? \" < > |"
    val etFilter = InputFilter { source, _, _, _, _, _ ->
        try {
            return@InputFilter if (blockCharacterSet.contains((source))) "" else source
        } catch (e: Exception) {
        }
        null
    }
    filters = arrayOf(etFilter)
}

fun Context.showToast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

fun Context.showToast(@StringRes id: Int) = showToast(resources.getString(id))

fun Activity.isActivityAlive(callback: (Activity) -> Unit) {
    try {
        if (isFinishing.not() &&
            isDestroyed.not()
        ) {
            callback(this)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun Fragment.isAlive(callback: (Activity) -> Unit) {
    if (activity != null && isAdded && !isDetached) {
        activity?.let { it.isActivityAlive { activity -> callback(activity) } }
    }
}

fun Activity.setStatusBarColor(@ColorRes colorId: Int) {
    try {
        window.decorView.systemUiVisibility =
            (if (colorId == R.color.white || colorId == R.color.background_light) {
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            } else {
                window.decorView.systemUiVisibility
                    .and(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv())
                    .and(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv())
            })

        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, colorId)
        window.navigationBarColor = ContextCompat.getColor(this, colorId)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun CardView.backgroundColor(@ColorRes colorId: Int) {
    setCardBackgroundColor(ContextCompat.getColor(context, colorId))
}

fun View.backgroundColor(@ColorRes colorId: Int) {
    setBackgroundColor(ContextCompat.getColor(context, colorId))
}

fun TextView.textColor(@ColorRes colorId: Int) {
    setTextColor(ContextCompat.getColor(context, colorId))
}

fun ImageView.colorFilter(@ColorRes colorId: Int) {
    setColorFilter(ContextCompat.getColor(context, colorId))
}

fun fromHtml(html: String?): Spanned? {
    return when {
        html == null -> {
            SpannableString("")
        }
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> {
            Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
        }
        else -> {
            Html.fromHtml(html)
        }
    }
}

fun splitFilename(fileName: String): Pair<String, String> {
    if (fileName.lastIndexOf(".") > 0) {
        val index = fileName.lastIndexOf(".")
        return Pair(fileName.substring(0, index), fileName.substring(index, fileName.length))
    }
    return Pair(fileName, "")
}

fun Context.isRTL(): Boolean {
    val config = resources.configuration
    return config.layoutDirection == View.LAYOUT_DIRECTION_RTL
}

inline fun <reified T : Any> Context.openActivity(vararg params: Pair<String, Any?>) {
    val intent = Intent(this, T::class.java)
    if (params.isNotEmpty()) fillIntentArguments(intent, params)
    startActivity(intent)
    (this as Activity).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
}

inline fun <reified T : Any> Context.getActivityIntent(vararg params: Pair<String, Any?>): Intent {
    val intent = Intent(this, T::class.java)
    if (params.isNotEmpty()) fillIntentArguments(intent, params)
    return intent
}

fun fillIntentArguments(intent: Intent, params: Array<out Pair<String, Any?>>) {
    params.forEach {
        when (val value = it.second) {
            is Int -> intent.putExtra(it.first, value)
            is Long -> intent.putExtra(it.first, value)
            is CharSequence -> intent.putExtra(it.first, value)
            is String -> intent.putExtra(it.first, value)
            is Float -> intent.putExtra(it.first, value)
            is Double -> intent.putExtra(it.first, value)
            is Char -> intent.putExtra(it.first, value)
            is Short -> intent.putExtra(it.first, value)
            is Boolean -> intent.putExtra(it.first, value)
            is Bundle -> intent.putExtra(it.first, value)
            is Parcelable -> intent.putExtra(it.first, value)
            is Array<*> -> when {
                value.isArrayOf<CharSequence>() -> intent.putExtra(it.first, value)
                value.isArrayOf<String>() -> intent.putExtra(it.first, value)
                value.isArrayOf<Parcelable>() -> intent.putExtra(it.first, value)
                else -> {}
            }
            is IntArray -> intent.putExtra(it.first, value)
            is LongArray -> intent.putExtra(it.first, value)
            is FloatArray -> intent.putExtra(it.first, value)
            is DoubleArray -> intent.putExtra(it.first, value)
            is CharArray -> intent.putExtra(it.first, value)
            is ShortArray -> intent.putExtra(it.first, value)
            is BooleanArray -> intent.putExtra(it.first, value)
            else -> {}
        }
        return@forEach
    }
}

fun Group.addOnClickListener(listener: (view: View) -> Unit) {
    referencedIds.forEach { id ->
        rootView.findViewById<View>(id).setOnClickListener(listener)
    }
}

fun Context.isInternetConnected(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    // For 29 api or above
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
                ?: return false
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            else -> false
        }
    }
    // For below 29 api
    else {
        if (connectivityManager.activeNetworkInfo != null && connectivityManager.activeNetworkInfo!!.isConnectedOrConnecting) {
            return true
        }
    }
    return false
}

@ChecksSdkIntAtLeast(parameter = 0)
fun isVersionLessThanEqualTo(version: Int): Boolean {
    return Build.VERSION.SDK_INT <= version
}

@ChecksSdkIntAtLeast(parameter = 0)
fun isVersionGreaterThanEqualTo(version: Int): Boolean {
    return Build.VERSION.SDK_INT >= version
}

fun Activity.navigateToSettings() {
    try {
        val dialogIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        dialogIntent.data = uri
        startActivityForResult(dialogIntent, SETTINGS_REQUEST_CODE)
    } catch (e: WindowManager.BadTokenException) {
        e.printStackTrace()
    }
}

fun Intent.isLaunchFromHistory(): Boolean =
    this.flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY == Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY

fun Context.getStringResourceByName(resName: String?): String {
    return try {
        val resId: Int = resources.getIdentifier(resName, "string", packageName)
        resources.getString(resId)
    } catch (ex: Exception) {
        ex.printStackTrace()
        "loading.."
    }
}

fun Context.sendEmail(recipient: String) {
    try {
        val intent = Intent(Intent.ACTION_SEND)
        val recipients = arrayOf(recipient)
        intent.putExtra(Intent.EXTRA_EMAIL, recipients)
//        intent.putExtra(Intent.EXTRA_SUBJECT, "Feedback: ${getString(R.string.app_name)}")
        intent.type = "text/plain"
        intent.setPackage("com.google.android.gm")
        startActivity(Intent.createChooser(intent, "Send mail"))
    } catch (ex: Exception) {
        ex.printStackTrace()
    }
}

fun Context.openPlayStore() {
    try {
        val uri = Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    } catch (ex: Exception) {
        ex.printStackTrace()
    }
}

fun Context.openBrowser(url: String) {
    try {
        val intent = Intent()
        intent.action = Intent.ACTION_VIEW
        intent.addCategory(Intent.CATEGORY_BROWSABLE)
        intent.data = Uri.parse(url)
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        e.printStackTrace()
        showToast(getString(R.string.no_browser_found))
    } catch (ex: Exception) {
        ex.printStackTrace()
    }
}

fun getFormattedDate(dateVal: Long): String {
    try {
        var date = dateVal
        date *= 1000L
        return SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date(date))
    } catch (ex: Exception) {
        ex.printStackTrace()
    }
    return ""

}

fun getReadableSize(size: Long): String {
    try {
        val symbols: DecimalFormatSymbols = DecimalFormatSymbols.getInstance(Locale.US)

        if (size <= 0) return "0"
        val units = arrayOf("B", "kB", "MB", "GB", "TB")
        val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
        return DecimalFormat(
            "#,##0.#",
            symbols
        ).format(size / 1024.0.pow(digitGroups.toDouble()))
            .toString() + " " + units[digitGroups]

    } catch (ex: Exception) {
        ex.printStackTrace()
    }
    return ""

}

fun formatString(text: Int): String {
    return try {
        String.format(Locale.getDefault(), "%d", text)
    } catch (e: Exception) {
        "0"
    }
}

fun formatString(text: String): String {
    return String.format(Locale.getDefault(), "%s", text)
}

@ColorInt
fun Context.color(@ColorRes res: Int): Int {
    return ContextCompat.getColor(this, res)
}

fun Activity.isSystemThemeDark(): Boolean {
    return when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
        Configuration.UI_MODE_NIGHT_NO -> false
        Configuration.UI_MODE_NIGHT_YES -> true
        else -> false
    }
}

fun Context.shareFile(filePath: String, extraText: String = "") {
    val file = File(filePath)
    val share = Intent(Intent.ACTION_SEND)
    share.type = "*/*"
    val pdfUri: Uri?
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        pdfUri = FileProvider.getUriForFile(this, packageName, file)
    } else {
        pdfUri = Uri.fromFile(file)
    }
    share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    share.putExtra(Intent.EXTRA_TEXT, extraText)
    share.putExtra(Intent.EXTRA_STREAM, pdfUri)
    startActivity(Intent.createChooser(share, getString(R.string.share_file)))
}

fun Context.saveImage(image: Bitmap): Uri? {
    //TODO - Should be processed in another thread
    val imagesFolder = File(cacheDir, "images")
    var uri: Uri? = null
    try {
        imagesFolder.mkdirs()
        val file = File(imagesFolder, "shared_image.png")
        val stream = FileOutputStream(file)
        image.compress(Bitmap.CompressFormat.PNG, 90, stream)
        stream.flush()
        stream.close()
        uri = FileProvider.getUriForFile(this, "com.horoscope.zodiac.astrology.stars", file)
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return uri
}

fun Context.shareImageBitmap(bitmap: Bitmap, caption: String = "") {
    val intent = Intent(Intent.ACTION_SEND)
    intent.putExtra(Intent.EXTRA_STREAM, saveImage(bitmap))
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    if (caption.isNotEmpty()) {
        intent.putExtra(Intent.EXTRA_TEXT, caption)
    }
    intent.type = "image/png"
    startActivity(intent)
}

fun Context.shareText(caption: String) {
    val intent = Intent(Intent.ACTION_SEND)
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    if (caption.isNotEmpty()) {
        intent.putExtra(Intent.EXTRA_TEXT, caption)
    }
    intent.type = "text/plain"
    startActivity(intent)
}

@SuppressLint("HardwareIds")
fun Context.getDeviceId(): String = Settings.Secure.getString(
    this.contentResolver,
    Settings.Secure.ANDROID_ID
)

fun ImageView.loadImage(url: Any?) {
    Glide.with(this).load(url).into(this)
}

fun Activity.getScreenSize(): Pair<Int, Int> {
    val displayMetrics = DisplayMetrics()
    windowManager.defaultDisplay.getMetrics(displayMetrics)
    return Pair(displayMetrics.widthPixels, displayMetrics.heightPixels)
}

fun View.showSnackBar(msg: String) {
    Snackbar.make(this, msg, Snackbar.LENGTH_INDEFINITE).show()
}

fun View.showSnackBar(@StringRes msg: Int) {
    Snackbar.make(
        this, msg, Snackbar.LENGTH_INDEFINITE
    ).show()
}

fun EditText.isEmailValid(): Boolean {
    val pattern: Pattern
    val emailPattern = "[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}"
    pattern = Pattern.compile(emailPattern)
    val matcher: Matcher = pattern.matcher(this.text.toString())
    return this.text.toString().isNotEmpty() && matcher.matches()
}

fun EditText.isEmpty(): Boolean {
    return this.text.toString().isEmpty()
}

fun EditText.showFieldRequiredError() {
    this.error = "Field Required"
}

fun EditText.textString(): String {
    return this.text.toString()
}

fun TextView.textString(): String = text.toString()

fun Context.getAppVersion(): String {
    try {
        val packageInfo = this.packageManager.getPackageInfo(this.packageName, 0)
        return packageInfo.versionName
    } catch (ex: Exception) {
    }
    return "1.0.0"
}

fun Activity.openEmailIntent() {
    try {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_APP_EMAIL)
        startActivity(intent)
    } catch (ex: Exception) {
    }
}

fun Activity.finishWithAnim() {
    this.finish()
    this.overridePendingTransition(R.anim.enter_anim, R.anim.exit_anim)
}

fun Activity.finishAffinityWithAnim() {
    this.finishAffinity()
    this.overridePendingTransition(R.anim.enter_anim, 0)
}

fun Activity.turnOnScreen() {
    try {
        val pm = this.getSystemService(Context.POWER_SERVICE) as PowerManager
        val isScreenOn = pm.isScreenOn
        if (!isScreenOn) {
            @SuppressLint("InvalidWakeLockTag") val wl =
                pm.newWakeLock(
                    PowerManager.FULL_WAKE_LOCK or
                            PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE,
                    "MyLock"
                )
            wl.acquire(20000)
            @SuppressLint("InvalidWakeLockTag") val wl_cpu =
                pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyCpuLock")
            wl_cpu.acquire(20000)
        }
    } catch (ex: Exception) {
        Log.e(TAG, "turnOnScreen: ", ex)
    }
}

fun Any.twoDecimalWithoutRound(): String {
    val df = DecimalFormat("#.##")
    df.roundingMode = RoundingMode.FLOOR
    return df.format(this.toString().toDouble())
}

fun Activity.handleYoutubeLink(deeplink: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(deeplink)
        this.startActivity(intent)
    } catch (ex: Exception) {
    }
}

fun Context.handleYoutubeLink(deeplink: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(deeplink)
        this.startActivity(intent)
    } catch (ex: Exception) {
    }
}

fun isAppVisible(): Boolean {
    return ProcessLifecycleOwner
        .get()
        .lifecycle
        .currentState
        .isAtLeast(Lifecycle.State.STARTED)
}

fun isAppKilled(): Boolean {
    return ProcessLifecycleOwner
        .get()
        .lifecycle
        .currentState
        .isAtLeast(Lifecycle.State.DESTROYED)
}

fun getCurrentTimeInMilliseconds(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.ENGLISH)
    val currentDateandTime = sdf.format(Date())
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z", Locale.ENGLISH)
        val localDate = LocalDateTime.parse(currentDateandTime, formatter)
        val timeInMilliseconds = localDate.atOffset(ZoneOffset.UTC).toInstant().toEpochMilli()
        return (timeInMilliseconds / 1000).toString()
    } else {
        try {
            val mDate = sdf.parse(currentDateandTime)
            val timeInMilliseconds = mDate.time
            println("Date in milli :: $timeInMilliseconds")
            return (timeInMilliseconds / 1000).toString()
        } catch (e: ParseException) {
            e.printStackTrace()
        }
    }
    return ""
}

fun getTimeDifference(oldTime: String, newTime: String): Int {
    return try {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.ENGLISH)
        val oldDateString = formatter.format(Date(oldTime.toLong() * 1000L))
        val newDateString = formatter.format(Date(newTime.toLong()))
        val oldDate = formatter.parse(oldDateString)
        val newDate = formatter.parse(newDateString)
        val timeDiff = newDate.time - oldDate.time
        (timeDiff / 1000).toString().toInt()
    } catch (e: Exception) {
        Log.e(TAG, "getTimeDifference: ", e)
    }
}

fun getAge(_year: Int, _month: Int, _day: Int): Int {
    val cal = GregorianCalendar()
    var a: Int
    val y: Int = cal[Calendar.YEAR]
    val m: Int = cal[Calendar.MONTH]
    val d: Int = cal[Calendar.DAY_OF_MONTH]
    cal[_year, _month] = _day
    a = y - cal[Calendar.YEAR]
    if (m < cal[Calendar.MONTH]
        || m == cal[Calendar.MONTH] && d < cal[Calendar.DAY_OF_MONTH]
    ) {
        --a
    }
    return a
}

fun getAge(date: String): Int {
    val splitString: Array<String> = date.split("-".toRegex()).toTypedArray()
    val year = splitString[0].toInt()
    val month = splitString[1].toInt()
    val day = splitString[2].toInt()
    val cal = GregorianCalendar()
    var a: Int
    val y: Int = cal[Calendar.YEAR]
    val m: Int = cal[Calendar.MONTH]
    val d: Int = cal[Calendar.DAY_OF_MONTH]
    cal[year, month] = day
    a = y - cal[Calendar.YEAR]
    if (m < cal[Calendar.MONTH]
        || m == cal[Calendar.MONTH] && d < cal[Calendar.DAY_OF_MONTH]
    ) {
        --a
    }
    return a
}

fun String.unescapeJavaString(): String {
    val sb = StringBuilder(this.length)
    var i = 0
    while (i < this.length) {
        var ch = this[i]
        if (ch == '\\') {
            val nextChar = if (i == this.length - 1) '\\' else this[i + 1]
            // Octal escape?
            if (nextChar in '0'..'7') {
                var code = "" + nextChar
                i++
                if (i < this.length - 1 && this[i + 1] >= '0' && this[i + 1] <= '7') {
                    code += this[i + 1]
                    i++
                    if (i < this.length - 1 && this[i + 1] >= '0' && this[i + 1] <= '7') {
                        code += this[i + 1]
                        i++
                    }
                }
                sb.append(code.toInt(8).toChar())
                i++
                continue
            }
            when (nextChar) {
                '\\' -> ch = '\\'
                'b' -> ch = '\b'
                'n' -> ch = '\n'
                'r' -> ch = '\r'
                't' -> ch = '\t'
                '\"' -> ch = '\"'
                '\'' -> ch = '\''
                'u' -> {
                    if (i >= this.length - 5) {
                        ch = 'u'
                        break
                    }
                    val code =
                        ("" + this[i + 2] + this[i + 3]
                                + this[i + 4] + this[i + 5]).toInt(16)
                    sb.append(Character.toChars(code))
                    i += 5
                    i++
                    continue
                }
            }
            i++
        }
        sb.append(ch)
        i++
    }
    return sb.toString()
}

fun String.parseHtmlString(): String {
    return HtmlCompat.fromHtml(
        this.unescapeJavaString(),
        HtmlCompat.FROM_HTML_MODE_LEGACY
    ).toString()
}

fun getThumbnailFromVideoURL(videoPath: String?): Bitmap? {
    try {
        var bitmap: Bitmap? = null
        var mediaMetadataRetriever: MediaMetadataRetriever? = null
        try {
            mediaMetadataRetriever = MediaMetadataRetriever()
            mediaMetadataRetriever.setDataSource(
                videoPath,
                HashMap()
            )

            bitmap = mediaMetadataRetriever.frameAtTime
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            throw Throwable(
                "Exception in retriveVideoFrameFromVideo(String videoPath)"
                        + e.message
            )
        } finally {
            mediaMetadataRetriever?.release()
        }
        return bitmap
    } catch (e: Exception) {
        Log.e(TAG, "getThumbnailFromVideoURL: ", e)
    }
    return null
}

fun Activity.overrideEnterAnimation() {
    this.overridePendingTransition(R.anim.enter_anim, 0);
}

fun Activity.overrideExitAnimation() {
    this.overridePendingTransition(0, R.anim.exit_anim)
}

fun getCurrentDateTimeString(): String {
    val sdf = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.getDefault())
    return sdf.format(Date())
}

fun Any.twoDecimal(): String {
    val df = DecimalFormat("#.##")
    df.roundingMode = RoundingMode.FLOOR
    return "%.2f".format(this.toString().toDouble())
//    return df.format(this.toString().toDouble())

}

fun String.toFormulatedTime(): String {
    val totalSecs = this.toInt()
    val hours: Int
    val minutes: Int
    val seconds: Int
    when {
        totalSecs in 1..59 -> {
            return "$totalSecs Sec"
        }
        totalSecs in 60..3599 -> {
            minutes = totalSecs % 3600 / 60
            seconds = totalSecs % 60
            return "$minutes Min $seconds sec"
        }
        totalSecs >= 3600 -> {
            hours = totalSecs / 3600
            minutes = totalSecs % 3600 / 60
            seconds = totalSecs % 60
            return "$hours hr $minutes Min $seconds sec"
        }
        else -> return ""
    }
}

fun String.toEpoch(): Long {
    var str = this
    str = str.trim { it <= ' ' }
    val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val date = df.parse(str)
    val epoch = date?.time!!
    println(epoch) // 1055545912454
    return epoch
}
fun getDefaultTimeZone(date: String): String {
    var ourDate = date
    try {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        val value = formatter.parse(ourDate)

        val dateFormatter =
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) //this format changeable
        dateFormatter.timeZone = TimeZone.getDefault()
        ourDate = dateFormatter.format(value!!)

    } catch (e: Exception) {
        Logs.e("Extension", "getDefaultTimeZone", e)
        ourDate = "00-00-0000 00:00"
    }
    return ourDate
}

fun String.toReferenceTime(): Long {
    val dateFormat = "yyyy-MM-dd HH:mm:ss"
    val defaultTimeZone: String = getDefaultTimeZone(this)
    val sdf = SimpleDateFormat(dateFormat, Locale.getDefault())
    var timeStamp: Long = 0
    timeStamp = sdf.parse(defaultTimeZone).time
    val cal = Calendar.getInstance()
    cal.time = Date(timeStamp)
    cal.timeZone = TimeZone.getDefault()
    val steTime = sdf.format(cal.time)
    return steTime.toEpoch()
}

fun List<*>.pageNumber(): Int {
    return ceil(this.size.toDouble() / 20).toInt()
}

fun getRandomString(): String {
    val r = Random()
    val i1 = r.nextInt(8000000) + 65
    val AlphaNumericString = ("0123456789"
            + "abcdefghijklmnopqrstuvxyz")
    val sb = java.lang.StringBuilder(i1)
    for (i in 0..20) {

        // generate a random number between
        // 0 to AlphaNumericString variable length
        val index = (AlphaNumericString.length
                * Math.random()).toInt()

        // add Character one by one in end of sb
        sb.append(
            AlphaNumericString[index]
        )
    }
    return "a_$sb"
}

fun View.gotoSettingsWithSnackBar() {
    Snackbar.make(
        this,
        "Above permission(s) needed. Please allow in your application settings.",
        Snackbar.LENGTH_INDEFINITE
    )
        .setAction("Settings") {
            val intent =
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri =
                Uri.fromParts("package", context.packageName, null)
            intent.data = uri
            context.startActivity(intent)
        }.show()
}

fun Context.requestSinglePermission(
    permission: String,
    callback: (Boolean) -> Unit
) {
    Dexter.withContext(this)
        .withPermission(permission)
        .withListener(object : PermissionListener {
            override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                callback(true)
            }

            override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                callback(false)
            }

            override fun onPermissionRationaleShouldBeShown(
                permission: PermissionRequest?,
                token: PermissionToken
            ) {
                token.continuePermissionRequest()
            }
        })
        .withErrorListener { dexterError ->
            Log.e(TAG, "requestMicrophonePermission: $dexterError")
        }.check()
}

fun Context.requestMultiplePermission(
    permissions: Collection<String>,
    callback: (Boolean) -> Unit
) {
    Dexter.withContext(this)
        .withPermissions(permissions)
        .withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                report?.let {
                    if (report.areAllPermissionsGranted()) {
                        callback(true)
                    }else{
                        callback(false)
                    }
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                permissions: MutableList<PermissionRequest>?,
                token: PermissionToken?
            ) {
                // Remember to invoke this method when the custom rationale is closed
                // or just by default if you don't want to use any custom rationale.
                token?.continuePermissionRequest()
            }
        })
        .withErrorListener {
            Log.e(TAG, "requestMultiplePermission: $it")
        }
        .check()
}



