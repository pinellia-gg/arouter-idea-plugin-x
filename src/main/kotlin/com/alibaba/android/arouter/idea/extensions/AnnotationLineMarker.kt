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
import com.intellij.psi.impl.source.PsiJavaCodeReferenceElementImpl
import com.intellij.psi.impl.source.tree.java.PsiAnnotationImpl
import com.intellij.psi.impl.source.tree.java.PsiJavaTokenImpl
import org.jetbrains.annotations.NotNull
import java.awt.event.MouseEvent

/**
 * 注解
 */
class AnnotationLineMarker : LineMarkerProvider, GutterIconNavigationHandler<PsiElement> {

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        return if (isARouterAnnotation(element)) {
            LineMarkerInfo(element,element.textRange, NavigationLineMarker.navigationOnIcon,null,this,GutterIconRenderer.Alignment.LEFT,
                { "ARouter Marker" })
        } else {
            null
        }
    }

    /**
     * 2023-12-11 00:22:42,360 [ 448344]
     * WARN - #c.i.c.d.LineMarkerInfo -
     * Performance warning:
     * LineMarker is supposed to be registered for leaf elements only, but got:
     * PsiAnnotation (class com.intellij.psi.impl.source.tree.java.PsiAnnotationImpl) instead.
     * First child: PsiJavaToken:AT (class com.intellij.psi.impl.source.tree.java.PsiJavaTokenImpl)
     */
    private fun isARouterAnnotation(element: PsiElement): Boolean {
//        System.out.println(element.javaClass.name +" | "+ element.text)
        if (element is PsiAnnotationImpl){
            val fullName = (element.children[1] as PsiJavaCodeReferenceElementImpl).qualifiedName
            return NavigationHelper.ROUTE_ANNOTATION_NAME == fullName
        }
        return false
    }

    override fun navigate(e: MouseEvent?, psiElement: PsiElement?) {
        if (psiElement is PsiAnnotationImpl){
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
        // Notify
        const val NOTIFY_SERVICE_NAME = "ARouter Plugin Tips"
        const val NOTIFY_TITLE = "Road Sign"
        const val NOTIFY_NO_TARGET_TIPS = "No usages found or unsupported type."

        val navigationOnIcon = IconLoader.getIcon("/icon/outline_my_location_black_18dp.png", AnnotationLineMarker::class.java)
    }

}