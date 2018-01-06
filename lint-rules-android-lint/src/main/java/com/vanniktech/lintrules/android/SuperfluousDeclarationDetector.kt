package com.vanniktech.lintrules.android

import com.android.SdkConstants.ANDROID_URI
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.LayoutDetector
import com.android.tools.lint.detector.api.XmlContext
import org.w3c.dom.Element
import java.util.HashSet

abstract class SuperfluousDeclarationDetector(
  private val issue: Issue,
  private val message: String,
  private val applicableSuperfluousAttributes: Collection<String>,
  private val replacement: String
) : LayoutDetector() {
  override fun getApplicableElements() = ALL

  override fun visitElement(context: XmlContext, element: Element) {
    val attributes = (0 until element.attributes.length)
        .map { element.attributes.item(it) }
        .filterNot { it.hasToolsNamespace() }
        .filter { applicableSuperfluousAttributes.contains(it.localName) }
        .map { it.nodeValue }
        .toList()

    if (attributes.size == applicableSuperfluousAttributes.size && HashSet<String>(attributes).size == 1) {
      val name = "Unify"
      val quickfix = fix()
        .group(
          *applicableSuperfluousAttributes.map { fix().name(name).unset(ANDROID_URI, it).build() }.toTypedArray(),
          fix().name(name).set(ANDROID_URI, replacement, attributes.first()).build()
        )

      context.report(issue, element, context.getLocation(element), message, quickfix)
    }
  }
}
