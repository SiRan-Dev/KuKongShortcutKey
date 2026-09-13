package com.sirandev.kukongshortcutkey

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast

const val KOOKONG_PACKAGE = "com.kookong.app"

/**
 * 透明中转页：由磁贴启动，在前台状态下再拉起酷控智能遥控，
 * 绕过 MIUI/HyperOS 对后台服务启动 Activity 的限制。
 */
class TransparentLauncherActivity : Activity() {

    private var launched = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 无 UI，等待 onResume 再启动，确保本应用已处于前台
    }

    override fun onResume() {
        super.onResume()
        if (launched) return
        launched = true

        val launchIntent = findKookongLaunchIntent()
        if (launchIntent != null) {
            launchIntent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
            )
            try {
                startActivity(launchIntent)
            } catch (e: Exception) {
                Toast.makeText(this, "启动酷控失败", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this, "未找到酷控智能遥控，请确认已安装", Toast.LENGTH_LONG).show()
        }
        finish()
    }

    private fun findKookongLaunchIntent(): Intent? {
        val pm = packageManager

        // 1) 标准包名
        pm.getLaunchIntentForPackage(KOOKONG_PACKAGE)?.let { return it }

        // 2) 包名包含 kookong
        val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        for (info in pm.queryIntentActivities(launcherIntent, 0)) {
            if (info.activityInfo.packageName.contains("kookong", ignoreCase = true)) {
                pm.getLaunchIntentForPackage(info.activityInfo.packageName)?.let { return it }
            }
        }

        // 3) 应用名包含“酷控”或 kookong
        for (info in pm.queryIntentActivities(launcherIntent, 0)) {
            val label = info.loadLabel(pm)?.toString() ?: continue
            if (label.contains("酷控") || label.contains("kookong", ignoreCase = true)) {
                pm.getLaunchIntentForPackage(info.activityInfo.packageName)?.let { return it }
            }
        }
        return null
    }
}
