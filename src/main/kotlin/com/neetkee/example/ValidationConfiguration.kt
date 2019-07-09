package com.neetkee.example

import com.fasterxml.jackson.databind.BeanDescription
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition
import org.hibernate.validator.HibernateValidatorConfiguration
import org.hibernate.validator.internal.engine.DefaultClockProvider
import org.hibernate.validator.spi.nodenameprovider.JavaBeanProperty
import org.hibernate.validator.spi.nodenameprovider.Property
import org.hibernate.validator.spi.nodenameprovider.PropertyNodeNameProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.validation.BindingResult
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import javax.validation.ClockProvider
import javax.validation.ConstraintViolation

@Configuration
class ValidationConfiguration(private val objectMapper: ObjectMapper) {
    @Bean
    fun validatorFactory(): LocalValidatorFactoryBean {
        return ValidatorFactoryBean(objectMapper)
    }
}

class ValidatorFactoryBean(private val objectMapper: ObjectMapper) : LocalValidatorFactoryBean() {

    override fun getClockProvider(): ClockProvider {
        return DefaultClockProvider.INSTANCE
    }

    override fun postProcessConfiguration(configuration: javax.validation.Configuration<*>) {
        if (configuration is HibernateValidatorConfiguration) {
            configuration.propertyNodeNameProvider(JacksonPropertyNodeNameProvider(objectMapper))
        }
        super.postProcessConfiguration(configuration)
    }

    override fun getRejectedValue(field: String, violation: ConstraintViolation<Any>, bindingResult: BindingResult): Any? {
        return violation.invalidValue
    }
}

class JacksonPropertyNodeNameProvider(private val objectMapper: ObjectMapper) : PropertyNodeNameProvider {

    override fun getName(property: Property): String {
        return if (property is JavaBeanProperty) {
            getJavaBeanPropertyName(property)
        } else getDefaultName(property)
    }

    private fun getJavaBeanPropertyName(property: JavaBeanProperty): String {
        val type = objectMapper.constructType(property.declaringClass)
        val description: BeanDescription = objectMapper.serializationConfig.introspect(type)
        val jsonName = description.findProperties()
                .filter { it.internalName == property.name }
                .map(BeanPropertyDefinition::getName)
                .firstOrNull()
        return jsonName ?: property.name
    }

    private fun getDefaultName(property: Property): String {
        return property.name
    }
}