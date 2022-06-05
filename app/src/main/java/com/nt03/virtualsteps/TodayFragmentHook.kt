package com.nt03.virtualsteps

import android.content.Context
import android.widget.Toast
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import java.time.LocalTime


class TodayFragmentHook(context: Context) {
    //构造函数
    init {
        hook(context)
    }

    companion object {
        private const val className = "com.ruidonghy.home.train.main.today.TodayFragment"//HOOK的类名

        fun hook(context: Context) {
            try {
                //获取要HOOK的类对象
                val clazz = XposedHelpers.findClass(className, context.classLoader)

                //屏蔽运动权限提示弹窗
                XposedHelpers.findAndHookMethod(clazz, "requestStepCounter",
                    object : XC_MethodHook() {
                        override fun beforeHookedMethod(param: MethodHookParam?) {
                            super.beforeHookedMethod(param)
                            if (param != null) {
                                XposedHelpers.setBooleanField(
                                    param.thisObject,
                                    "needShowPermission",
                                    false
                                )
                            }
                        }
                    })


                //修改App获取到的步数
                XposedHelpers.findAndHookMethod(clazz, "sendStepToServer", Int::class.java,
                    object : XC_MethodHook() {
                        override fun beforeHookedMethod(param: MethodHookParam) {
                            super.beforeHookedMethod(param)

                            // 根据当前时间设置修改的最大步数
                            val maxStep = when (val hour = LocalTime.now().hour) {
                                in 0..7 -> 19999
                                8 -> 25999
                                in 9..13 -> (hour - 4) * 6000 + 2000
                                else -> 58000
                            }
                            //修改参数为最大步数
                            param.args[0] = maxStep
                            //修改获取的手机传感器步数为最大步数
                            XposedHelpers.setIntField(
                                param.thisObject, "mPhoneStep", maxStep
                            )
                            //修改总步数为最大步数
                            XposedHelpers.setIntField(
                                param.thisObject, "mTotalStep", maxStep
                            )
                        }
                    })
            } catch (e: Error) {
                Toast.makeText(context, e.stackTraceToString(), Toast.LENGTH_LONG).show()
            }
        }
    }
}