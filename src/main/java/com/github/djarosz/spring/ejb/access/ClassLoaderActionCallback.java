package com.github.djarosz.spring.ejb.access;

public interface ClassLoaderActionCallback<T> {

	T execute() throws Throwable;

}

