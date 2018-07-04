package org.jctools.util;

public interface JvmInfo {
    int CACHE_LINE_SIZE = Integer.getInteger("jctools.cacheLineSize", 64);
    @SuppressWarnings("restriction")
	int PAGE_SIZE = UnsafeAccess.UNSAFE.pageSize();
    int CPUs = Runtime.getRuntime().availableProcessors();
}
