package com.lestere.utils

import com.lestere.model.RequestInfo
import io.ktor.client.statement.*
import kotlinx.serialization.json.Json
import org.reflections.Reflections
import org.reflections.scanners.Scanners
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import org.reflections.util.FilterBuilder
import java.lang.reflect.Modifier.isStatic
import java.net.URLEncoder
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.isSubclassOf

/**
 * format json string to json format
 */
fun Json.formatText(jsonString: String): String = runCatching {
    val jsonElement = parseToJsonElement(jsonString)
    encodeToString(jsonElement)
}.getOrElse { jsonString }

/**
 * Encode url components
 */
fun String.encodeURLComponent() = URLEncoder.encode(this, "UTF-8")
    .replace("+", "%20")
    .replace("%7E", "~")
    .replace("%2F", "/")

/**
 * Find all instances of the specified KCPass type inside specified type
 */
fun <T : Any> KClass<T>.findAllNestedInstances(): List<T> {
    val result = mutableListOf<T>()

    // Recursively look up single instances in all nested classes
    fun findInstancesIn(kClass: KClass<*>): List<T> {
        val instances = mutableListOf<T>()

        kClass.nestedClasses
            .filter { it.isFinal && it.objectInstance != null }
            .forEach { nestedClass ->
                nestedClass.objectInstance?.let { instance ->
                    if (isInstance(instance)) {
                        @Suppress("UNCHECKED_CAST")
                        instances.add(instance as T)
                    }
                    // Recursively find deeper nesting
                    instances.addAll(findInstancesIn(nestedClass))
                }
            }

        return instances
    }

    // Search from the current class
    result.addAll(findInstancesIn(this))
    return result
}

/**
 * Find all instances of the specified KCPass type, including nested and inner classes, sealed class subclasses, and data objects
 */
fun <T : Any> KClass<T>.findAllInstances(packageName: String = ""): List<T> {
    val results = mutableListOf<T>()

    try {
        // If it is a sealed class, directly obtain all its subclasses
        if (this.isSealed) {
            this.sealedSubclasses.forEach { subclass ->
                // For object and data object, directly obtain their singleton instances
                subclass.objectInstance?.let { instance ->
                    results.add(instance)
                } ?: run {
                    // For non object subclasses, try instantiation
                    if (!subclass.isAbstract && !subclass.java.isInterface) {
                        try {
                            val instance = subclass.createInstance()
                            results.add(instance)
                        } catch (e: Exception) {
                            println("Unable to instantiated sealed class subclass ${subclass.simpleName}: ${e.message}")
                        }
                    }
                }

                // Recursive processing of nested sealed classes
                if (subclass.isSealed) {
                    val nestedInstances = subclass.findAllInstances(packageName)
                    results.addAll(nestedInstances)
                }
            }

            // If the direct subclass of sealed class has already been processed, return the result in advance
            if (results.isNotEmpty()) {
                return results
            }
        }

        // Configure Reflections to scan all classes (including nested and inner classes)
        val configBuilder = ConfigurationBuilder()
            .setScanners(Scanners.SubTypes.filterResultsBy { true }, Scanners.TypesAnnotated)
            .setUrls(ClasspathHelper.forClassLoader())

        // If a package name is specified, the scanning range is restricted
        if (packageName.isNotEmpty()) {
            configBuilder
                .setUrls(ClasspathHelper.forPackage(packageName))
                .filterInputsBy(FilterBuilder().includePackage(packageName))
        }

        val reflections = Reflections(configBuilder)

        // Retrieve all possible classes (including nested and inner classes)
        val allClasses = mutableSetOf<Class<*>>()

        // First, obtain all subclasses of the specified type
        allClasses.addAll(reflections.getSubTypesOf(this.java))

        // Then scan all classes to find nested and inner classes
        val allTypesScanner = Reflections(
            ConfigurationBuilder()
                .setScanners(Scanners.SubTypes)
                .setUrls(ClasspathHelper.forClassLoader())
        )

        val allTypes = allTypesScanner.getAll(Scanners.SubTypes)
            .flatMap { it.split(",") }
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        // Load all classes and check if they are compatible with the target type
        for (typeName in allTypes) {
            try {
                val type = Class.forName(typeName)
                if (this.java.isAssignableFrom(type)) {
                    allClasses.add(type)
                }

                // Get all inner and nested classes
                type.declaredClasses.forEach { innerClass ->
                    if (this.java.isAssignableFrom(innerClass)) {
                        allClasses.add(innerClass)
                    }
                }
            } catch (_: Exception) {
                // Ignore classes that cannot be loaded
            }
        }

        // Instantiate all classes found
        for (clazz in allClasses) {
            val kotlinClass = clazz.kotlin

            // Exclude interfaces, abstract classes, and anonymous classes
            if (!kotlinClass.isAbstract &&
                !clazz.isInterface &&
                !clazz.isAnonymousClass &&
                kotlinClass.isSubclassOf(this)
            ) {

                try {
                    // Attempt to obtain object instances (for object and data object singleton)
                    kotlinClass.objectInstance?.let { instance ->
                        @Suppress("UNCHECKED_CAST")
                        results.add(instance as T)
                        return@let
                    }

                    // Attempt to use a non-parametric constructor instantiation
                    val constructor = clazz.constructors.firstOrNull { it.parameterCount == 0 }
                    if (constructor != null) {
                        constructor.isAccessible = true
                        @Suppress("UNCHECKED_CAST")
                        results.add(constructor.newInstance() as T)
                    } else {
                        // For inner classes, try to find constructors with external class parameters
                        if (clazz.isMemberClass && !isStatic(clazz.modifiers)) {
                            val outerClass = clazz.declaringClass
                            val outerInstance = findOrCreateInstance(outerClass)
                            if (outerInstance != null) {
                                val innerConstructor =
                                    clazz.constructors.firstOrNull { it.parameterCount == 1 && it.parameterTypes[0] == outerClass }
                                if (innerConstructor != null) {
                                    innerConstructor.isAccessible = true
                                    @Suppress("UNCHECKED_CAST")
                                    results.add(innerConstructor.newInstance(outerInstance) as T)
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    println("Cannot be instantiated ${clazz.name}: ${e.message}")
                }
            }
        }

        // Additional processing: Find all data objects and object implementations
        findAllObjectImplementations(packageName).forEach { instance ->
            results.add(instance)
        }
    } catch (e: Exception) {
        println("Error occurred while searching for instances: ${e.message}")
    }

    return results
}

/**
 * Find all object and data object implementations of the specified type
 */
private fun <T : Any> KClass<T>.findAllObjectImplementations(packageName: String = ""): List<T> {
    val results = mutableListOf<T>()

    try {
        // Configure Reflections to scan all classes under the specified package
        val configBuilder = ConfigurationBuilder()
            .setScanners(Scanners.SubTypes)
            .setUrls(ClasspathHelper.forClassLoader())

        if (packageName.isNotEmpty()) {
            configBuilder
                .setUrls(ClasspathHelper.forPackage(packageName))
                .filterInputsBy(FilterBuilder().includePackage(packageName))
        }

        val reflections = Reflections(configBuilder)

        // Get all subclasses
        val subTypes = reflections.getSubTypesOf(this.java)

        // Traverse all subclasses, search for object and data object
        for (subType in subTypes) {
            try {
                val kotlinClass = subType.kotlin

                // Obtain singleton instances of object and data object
                kotlinClass.objectInstance?.let { instance ->
                    results.add(instance)
                }
            } catch (_: Exception) {
                // Ignore exceptions
            }
        }
    } catch (e: Exception) {
        println("Error occurred while searching for object implementation: ${e.message}")
    }

    return results
}

/**
 * Find or create instances of a class
 */
private fun findOrCreateInstance(clazz: Class<*>): Any? = run {
    // Attempt to obtain object instances (for object singleton)
    clazz.kotlin.objectInstance?.let { return it }

    // Attempt to use a non-parametric constructor instantiation
    val constructor = clazz.constructors.firstOrNull { it.parameterCount == 0 }
    if (constructor != null) {
        constructor.isAccessible = true
        return constructor.newInstance()
    }
}

val HttpResponse.requestInfo: RequestInfo
    get() = RequestInfo(
        request.url.toString(),
        status,
        requestTime.timestamp,
        responseTime.timestamp
    )
