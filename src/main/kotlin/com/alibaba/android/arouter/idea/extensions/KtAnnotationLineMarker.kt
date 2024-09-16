package com.alibaba.android.arouter.idea.extensions

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.util.IconLoader
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.impl.source.tree.java.PsiJavaTokenImpl
import com.intellij.psi.util.elementType
import com.intellij.psi.util.nextLeaf
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
            LineMarkerInfo(element,
                element.textRange,
                NavigationLineMarker.navigationOnIcon,
                null,
                this,
                GutterIconRenderer.Alignment.LEFT,
                { "ARouter Marker" })
        } else {
            null
        }
    }

    fun printElement(element: PsiElement) {
        println(element.javaClass.name + "  element class:${element::javaClass.name} " +
                ",  element:$element,  text: ${element.text} , elementType:${element.elementType}," +
                ",typeCalss: " + element.elementType?.javaClass?.name)


    }

    fun printNextElement(element: PsiElement?) {
        println(element?.javaClass?.name + "found @ ,next element class:${element ?: javaClass.name} ,  element:$element,  text: ${element?.text}, elementType:${element.elementType}")
    }

    private fun isARouterAnnotation(element: PsiElement): Boolean {
//        printElement(element)
        if (element is LeafPsiElement && element.text == "@") {
            val nextElement = element.nextLeaf(false)

            return nextElement is LeafPsiElement && nextElement.text == "Route"
        }

        return false
    }

    override fun navigate(e: MouseEvent?, psiElement: PsiElement?) {
        if (psiElement is LeafPsiElement) {
            val hasFind = NavigationHelper.findUsagesMethod(psiElement.parent, e)
            if (!hasFind) {
                notifyNotFound()
            }
        }
    }

    private fun notifyNotFound() {
        Notifications.Bus.notify(
            Notification(
                NOTIFY_SERVICE_NAME,
                NOTIFY_TITLE,
                NOTIFY_NO_TARGET_TIPS,
                NotificationType.WARNING
            )
        )
    }

    override fun collectSlowLineMarkers(
        elements: @NotNull MutableList<out PsiElement>,
        result: @NotNull MutableCollection<in LineMarkerInfo<*>>
    ) {
    }


    companion object {
        const val SDK_NAME = "ARouter"

        // Notify
        const val NOTIFY_SERVICE_NAME = "ARouter Plugin Tips"
        const val NOTIFY_TITLE = "Road Sign"
        const val NOTIFY_NO_TARGET_TIPS = "No destination found or unsupported type."

//        val navigationOnIcon =
//            IconLoader.getIcon("/icon/outline_my_location_black_18dp.png", KtAnnotationLineMarker::class.java)
    }

}