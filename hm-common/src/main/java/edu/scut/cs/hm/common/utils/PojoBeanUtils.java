package edu.scut.cs.hm.common.utils;

import edu.scut.cs.hm.common.pojo.PojoClass;
import edu.scut.cs.hm.common.pojo.Property;
import org.apache.commons.beanutils.ConvertUtilsBean2;
import org.apache.commons.beanutils.PropertyUtilsBean;

import java.lang.reflect.Method;

public final class PojoBeanUtils {

    private PojoBeanUtils() {}

    private static final PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();
    private static final ConvertUtilsBean2 convertUtilsBean = new ConvertUtilsBean2();

    public static Object getValue(Object bean, String name) {
        try {
            return propertyUtilsBean.getNestedProperty(bean, name);
        } catch (Exception e) {
            return null;
        }
    }


    public static Object convert(String value, Class type) {
        return convertUtilsBean.convert(value, type);
    }

    /**
     * Copy properties into lombok-style builders (it builder do not follow java bean convention)
     * @param src source bean object
     * @param dst destination builder object
     * @return dst object
     */
    public static <T> T copyToBuilder(Object src, T dst) {
        PojoClass srcpojo = new PojoClass(src.getClass());
        Class<?> builderClass = dst.getClass();
        Method[] methods = builderClass.getMethods();
        for(Method method: methods) {
            boolean isBuilderSetter = method.getReturnType().equals(builderClass) &&
                    method.getParameterCount() == 1;
            if(!isBuilderSetter) {
                continue;
            }
            String propertyName = method.getName();
            Property property = srcpojo.getProperties().get(propertyName);
            if(property == null) {
                continue;
            }
            Object val = property.get(src);
            if(val == null) {
                continue;
            }
            try {
                method.invoke(dst, val);
            } catch (Exception e) {
                //nothing
            }
        }
        return dst;
    }
}