package com.nt03.virtualsteps

import android.content.Context
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage


class MainHookLoader : IXposedHookLoadPackage {

    private val packageName = "com.ruidonghy.will"  //软件包名
    private val className = "com.stub.StubApp"      //360加固 HOOK的类名
    private val methodName = "attachBaseContext"    //360加固 HOOK的方法名


    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam?) {
        //检查是否为指定的APP
        if (lpparam?.packageName != packageName) return
        //获得ClassLoader
        val classLoader = lpparam.classLoader
        try {
            //未加壳 直接执行HOOK
            TodayFragmentHook(classLoader)
        } catch (e: XposedHelpers.ClassNotFoundError) {
            //从360加固中获取真ClassLoader
            XposedHelpers.findAndHookMethod(className, classLoader, methodName, Context::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam?) {
                        super.afterHookedMethod(param)
                        //获取到Context
                        val context = param?.args?.get(0) as Context
                        //执行HOOK
                        TodayFragmentHook(context.classLoader)
                    }
                })
        } catch (e: Error) {
            XposedBridge.log(e.stackTraceToString())
        }
    }
}