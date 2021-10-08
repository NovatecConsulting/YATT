package com.novatecgmbh.eventsourcing.axon.security

import org.springframework.core.MethodParameter
import org.springframework.core.annotation.AnnotationUtils.findAnnotation
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

class UserPrincipalResolver : HandlerMethodArgumentResolver {
  override fun supportsParameter(parameter: MethodParameter) =
      findMethodAnnotation(AuthenticationPrincipal::class.java, parameter) != null &&
          (parameter.parameterType.isAssignableFrom(UnregisteredUserPrincipal::class.java) ||
              parameter.parameterType.isAssignableFrom(RegisteredUserPrincipal::class.java))

  @Throws(Exception::class)
  override fun resolveArgument(
      parameter: MethodParameter,
      mavContainer: ModelAndViewContainer?,
      webRequest: NativeWebRequest,
      binderFactory: WebDataBinderFactory?
  ): Any? {
    val authentication = SecurityContextHolder.getContext().authentication ?: return null
    val principal = authentication.principal
    val parameterType = parameter.parameterType

    // TODO map exceptions to correct status codes
    return when {
      principal is RegisteredUserPrincipal &&
          parameterType.isAssignableFrom(RegisteredUserPrincipal::class.java) -> principal
      principal is RegisteredUserPrincipal &&
          parameterType.isAssignableFrom(UnregisteredUserPrincipal::class.java) ->
          throw IllegalStateException(
              "Tried to resolve unregistered user principal, but user is already registered")
      principal is UnregisteredUserPrincipal &&
          parameterType.isAssignableFrom(UnregisteredUserPrincipal::class.java) -> principal
      principal is UnregisteredUserPrincipal &&
          parameterType.isAssignableFrom(RegisteredUserPrincipal::class.java) ->
          throw IllegalStateException(
              "Tried to resolve registered user principal, but user is not registered yet")
      principal !is UnregisteredUserPrincipal && principal !is RegisteredUserPrincipal ->
          throw IllegalStateException("Unknown principal type received from security context")
      else ->
          throw Exception(
              "Cannot resolve $parameterType, principal type from security context is: ${principal::class}")
    }
  }
}

/**
 * Obtains the specified [Annotation] on the specified [MethodParameter].
 *
 * @param annotationClass the class of the [Annotation] to find on the [ ]
 * @param parameter the [MethodParameter] to search for an [Annotation]
 * @return the [Annotation] that was found or null.
 */
private fun <T : Annotation> findMethodAnnotation(
    annotationClass: Class<T>,
    parameter: MethodParameter
): T? {
  var annotation = parameter.getParameterAnnotation(annotationClass)
  if (annotation != null) {
    return annotation
  }
  val annotationsToSearch = parameter.parameterAnnotations
  for (toSearch in annotationsToSearch) {
    annotation = findAnnotation(toSearch.annotationClass.java, annotationClass)
    if (annotation != null) {
      return annotation
    }
  }
  return null
}
