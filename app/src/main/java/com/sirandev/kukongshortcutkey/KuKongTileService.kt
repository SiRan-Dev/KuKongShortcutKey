package com.sirandev.kukongshortcutkey

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.Toast

/**
 * 快捷设置磁贴：默认熄灭，点击时短暂点亮并打开酷控智能遥控。
 * 通过透明中转 Activity 启动酷控，规避后台启动限制。
 */
class KuKongTileService : TileService() {

    private val handler = Handler(Looper.getMainLooper())

    override fun onStartListening() {
        super.onStartListening()
        setTileState(Tile.STATE_INACTIVE)
    }

    override fun onClick() {
        super.onClick()
        if (isLocked()) {
            unlockAndRun { flashAndLaunch() }
        } else {
            flashAndLaunch()
        }
    }

    private fun flashAndLaunch() {
        setTileState(Tile.STATE_ACTIVE)
        handler.postDelayed({
            openLauncherActivity()
            setTileState(Tile.STATE_INACTIVE)
        }, 150)
    }

    private fun setTileState(state: Int) {
        qsTile?.apply {
            if (this.state != state) {
                this.state = state
                updateTile()
            }
        }
    }

    private fun openLauncherActivity() {
        val intent = Intent(this, TransparentLauncherActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            // startActivityAndCollapse(PendingIntent) 是 API 34 新增的重载；
            // API 33 及以下必须走旧的 Intent 版本，否则 NoSuchMethodError
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                val pendingIntent = PendingIntent.getActivity(
                    this, 0, intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
                startActivityAndCollapse(pendingIntent)
            } else {
                @Suppress("DEPRECATION")
                startActivityAndCollapse(intent)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "启动失败，请检查“后台弹出界面”权限", Toast.LENGTH_LONG).show()
        }
    }
}
