package com.example.excuseencyclopedia.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.excuseencyclopedia.MainActivity
import com.example.excuseencyclopedia.R

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "daily_excuse_channel"

        // 1. 알림 채널 만들기 (안드로이드 8.0 이상 필수)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "매일 변명 알림",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "변명 기록을 잊지 않도록 매일 알려줍니다."
            }
            notificationManager.createNotificationChannel(channel)
        }

        // 2. 알림 클릭하면 앱 열기
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, contentIntent, PendingIntent.FLAG_IMMUTABLE
        )

        // 3. 알림 만들기
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // 기본 아이콘 (나중에 앱 아이콘으로 바꾸세요)
            .setContentTitle("오늘은 할 일 다 하셨나요?")
            .setContentText("하루를 마무리하며 오늘의 핑계를 기록해보세요!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        // 4. 알림 띄우기
        notificationManager.notify(1001, notification)
    }
}