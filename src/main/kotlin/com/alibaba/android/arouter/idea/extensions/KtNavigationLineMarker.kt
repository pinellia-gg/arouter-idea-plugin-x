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
import com.intellij.psi.*
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.idea.caches.resolve.resolveToCall
import org.jetbrains.kotlin.load.java.lazy.descriptors.LazyJavaClassDescriptor
import org.jetbrains.kotlin.load.java.structure.impl.JavaClassImpl
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.psiUtil.getCallNameExpression
import java.awt.event.MouseEvent

/**
 * Mark navigation target.
 *
 * @author zhilong <a href="mailto:zhilong.liu@aliyun.com">Contact me.</a>
 * @version 1.0
 * @since 2018/12/13 12:30 PM
 */
class KtNavigationLineMarker : LineMarkerProvider, GutterIconNavigationHandler<PsiElement> {

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
//        System.out.println(element.javaClass.name +" | "+ element.text)
        return if (isNavigationCall(element)) {
            LineMarkerInfo<PsiElement>(element, element.textRange, navigationOnIcon,
                    Pass.UPDATE_ALL, null, this,
                    GutterIconRenderer.Alignment.LEFT)
        } else {
            null
        }
    }

    override fun navigate(e: MouseEvent?, psiElement: PsiElement?) {
        if (psiElement is KtCallExpression){
            val arguments = psiElement.valueArguments
            if (arguments.size == 1){
                val targetPath = arguments[0].text.replace("\"", "")
                val found = NavigationHelper.findTargetAndNavigate(psiElement, targetPath, e)
                if (found){
                    return
                }
            }
        }

        notifyNotFound()
    }

    private fun notifyNotFound() {
        Notifications.Bus.notify(Notification(NOTIFY_SERVICE_NAME, NOTIFY_TITLE, NOTIFY_NO_TARGET_TIPS, NotificationType.WARNING))
    }

    override fun collectSlowLineMarkers(elements: MutableList<PsiElement>, result: MutableCollection<LineMarkerInfo<PsiElement>>) {}

    /**
     * Judge whether the code used for navigation.
     */
    private fun isNavigationCall(psiElement: PsiElement): Boolean {
        if (psiElement is KtCallExpression) {
//            System.out.println("target=>" + psiElement.javaClass.name +" | "+ psiElement.text)

            val method = psiElement.getCallNameExpression() ?: return false

            if (method.getReferencedName() == "build") {
                if (isClassOfARouter(findContainingClassName(psiElement))) {
                    return true
                }
            }
        }
        return false
    }

    private fun findContainingClassName(callExpression: KtCallExpression): PsiClass? {
        val callableDescriptor = callExpression.resolveToCall()?.resultingDescriptor ?: return null
        val classDescriptor = callableDescriptor.containingDeclaration as? ClassDescriptor ?: return null
        return ((classDescriptor as LazyJavaClassDescriptor).jClass as JavaClassImpl).psi
    }

    /**
     * Judge whether the caller was ARouter
     */
    private fun isClassOfARouter(psiClass: PsiClass?): Boolean {
        if (psiClass == null){
            return false
        }
        // It was ARouter
        if (psiClass.name.equals(SDK_NAME)) {
            return true
        }

        // It super class was ARouter
        psiClass.supers.find { it.name == SDK_NAME } ?: return false

        return true
    }

    companion object {
        const val SDK_NAME = "ARouter"

        // Notify
        const val NOTIFY_SERVICE_NAME = "ARouter Plugin Tips"
        const val NOTIFY_TITLE = "Road Sign"
        const val NOTIFY_NO_TARGET_TIPS = "No destination found or unsupported type."

        val navigationOnIcon = IconLoader.getIcon("/icon/outline_my_location_black_18dp.png")
    }

}