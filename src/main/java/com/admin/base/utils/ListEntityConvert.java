package com.admin.base.utils;

import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ZXX
 * @version 1.0
 * @date 2021/9/12 7:37 下午
 * @desc List<T> 转换成List<F>
 */
public class ListEntityConvert {
    public static <T, F> List<F> listCopyToAnotherList(Class<F> target, List<T> sourcesList) throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        if (!CollectionUtils.isEmpty(sourcesList)) {
            List<F> fList = new ArrayList<>();
            for (T t : sourcesList) {
                F f = target.getDeclaredConstructor().newInstance();
                BeanUtils.copyProperties(t, f);
                fList.add(f);
            }
            return fList;
        }
        return null;
    }


}
