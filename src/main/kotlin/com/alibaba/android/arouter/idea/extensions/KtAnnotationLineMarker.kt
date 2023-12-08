package com.alibaba.android.arouter.idea.extensions

import com.intellij.codeHighlighting.Pass
import com.intellij.codeInsight.daemon.GutterIconNavigationHandler
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.util.IconLoader
import com.intellij.psi.PsiElement
import org.jetbrains.annotations.NotNull
import org.jetbrains.kotlin.asJava.toLightAnnotation
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import java.awt.event.MouseEvent

/**
 * 注解
 */
class KtAnnotationLineMarker : LineMarkerProvider, GutterIconNavigationHandler<PsiElement> {

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        return if (isARouterAnnotation(element)) {
            LineMarkerInfo(element,element.textRange, NavigationLineMarker.navigationOnIcon,null,this,GutterIconRenderer.Alignment.LEFT,
                { "ARouter Marker" })
        } else {
            null
        }
    }

    private fun isARouterAnnotation(element: PsiElement): Boolean {
//        System.out.println(element.javaClass.name +" | "+ element.text)
        if (element is KtAnnotationEntry){
            val fullName = element.toLightAnnotation()?.qualifiedName
            return NavigationHelper.ROUTE_ANNOTATION_NAME == fullName
        }
        return false
    }

    override fun navigate(e: MouseEvent?, psiElement: PsiElement?) {
        if(psiElement is KtAnnotationEntry){
            val hasFind = NavigationHelper.findUsagesMethod(psiElement, e)
            if (!hasFind){
                notifyNotFound()
            }
        }
    }

    private fun notifyNotFound() {
        Notifications.Bus.notify(Notification(NOTIFY_SERVICE_NAME, NOTIFY_TITLE, NOTIFY_NO_TARGET_TIPS, NotificationType.WARNING))
    }

    override fun collectSlowLineMarkers(
        elements: @NotNull MutableList<out PsiElement>,
        result: @NotNull MutableCollection<in LineMarkerInfo<*>>
    ) {}


    companion object {
        const val SDK_NAME = "ARouter"

        // Notify
        const val NOTIFY_SERVICE_NAME = "ARouter Plugin Tips"
        const val NOTIFY_TITLE = "Road Sign"
        const val NOTIFY_NO_TARGET_TIPS = "No destination found or unsupported type."

        val navigationOnIcon = IconLoader.getIcon("/icon/outline_my_location_black_18dp.png", KtAnnotationLineMarker::class.java)
    }

}