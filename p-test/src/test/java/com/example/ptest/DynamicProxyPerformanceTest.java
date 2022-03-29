//package com.example.ptest;
//
//import org.springframework.cglib.proxy.MethodProxy;
//import org.springframework.cglib.proxy.MethodInterceptor;
//import org.springframework.cglib.proxy.Enhancer;
//import org.springframework.cglib.core.DebuggingClassWriter;
//import org.openjdk.jmh.annotations.Benchmark;
//import org.openjdk.jmh.annotations.OutputTimeUnit;
//import org.openjdk.jmh.annotations.Scope;
//import org.openjdk.jmh.annotations.State;
//import org.openjdk.jmh.runner.Runner;
//import org.openjdk.jmh.runner.options.Options;
//import org.openjdk.jmh.runner.options.OptionsBuilder;
//import org.springframework.cglib.reflect.FastClass;
//
//import java.lang.reflect.InvocationHandler;
//import java.lang.reflect.InvocationTargetException;
//import java.lang.reflect.Method;
//import java.lang.reflect.Proxy;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//import java.util.concurrent.TimeUnit;
//
//@OutputTimeUnit(TimeUnit.SECONDS)
//@State(Scope.Thread)
//public class DynamicProxyPerformanceTest {
//
//    static CountService jdkProxy;
//
//    static CountService cglibProxy;
//
//    static FastClass fastClass;
//
//    static Class countClazz;
//
//    static String[] methods;
//
//    static CountService delegate;
//
//    static Class<?>[] argsTypes = new Class<?>[0];
//    static Object[] args = new Object[0];
//
//    static int methodsNum = 6;
//
//    static {
//        try {
//            invokeInit();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static String[] getAllMethods(Class<?> clazz) {
//        List<Method> methodList = new ArrayList<>();
//        while (clazz != null) {
//            methodList.addAll(new ArrayList<>(Arrays.asList(clazz.getMethods())));
//            clazz = clazz.getSuperclass();
//        }
//        String[] methodName = new String[methodList.size()];
//        int i = 0;
//        for (Method method : methodList) {
//            methodName[i++] = method.getName();
//        }
//        return methodName;
//    }
//
//    public static void invokeInit() throws Exception {
//
//        delegate = new CountServiceImpl();
//        countClazz = delegate.getClass();
//        methods = getAllMethods(countClazz);
//        System.out.println("methodsNum: " + methods.length+" : "+methodsNum);
//        fastClass = FastClass.create(delegate.getClass());
//    }
//
//    public static void init() throws Exception {
//        CountService delegate = new CountServiceImpl();
//
//        long time = System.currentTimeMillis();
//        jdkProxy = createJdkDynamicProxy(delegate);
//        time = System.currentTimeMillis() - time;
//        System.out.println("Create JDK Proxy: " + time + " ms");
//
//        time = System.currentTimeMillis();
//        cglibProxy = createCglibDynamicProxy(delegate);
//        fastClass = FastClass.create(delegate.getClass());
//        time = System.currentTimeMillis() - time;
//        System.out.println("Create CGLIB Proxy: " + time + " ms");
//    }
//
//    public static void main(String[] args) throws Exception {
//        System.setProperty(DebuggingClassWriter.DEBUG_LOCATION_PROPERTY, ".//");
//        try {
//            init();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        Options opt = new OptionsBuilder()
//                .include(DynamicProxyPerformanceTest.class.getSimpleName())
//                .forks(1) // 用一个进程
//                .warmupIterations(3) // 预热次数
//                .measurementIterations(5) // 测试次数
//                .build();
//        new Runner(opt).run();
//    }
//
//
//    @Benchmark
//    public void cglibInvoke() throws InvocationTargetException {
//        int count = 1000000;
//        for (int i = 0; i < count; i++) {
//            for (int j = 0; j < methodsNum; j++) {
//                int methodIndex = fastClass.getIndex(methods[j], argsTypes);
//                fastClass.invoke(methodIndex, delegate, args);
//            }
//        }
//    }
//
//    @Benchmark
//    public void jdkInvoke() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
//        int count = 1000000;
//        for (int i = 0; i < count; i++) {
//            for (int j = 0; j < methodsNum; j++) {
//                Method m1 = countClazz.getDeclaredMethod(methods[j]);
//                m1.invoke(delegate);
//            }
//        }
//    }
//
//    private static <T extends CountService> CountService createJdkDynamicProxy(
//            final CountService delegate) {
//        CountService jdkProxy = (CountService) Proxy
//                .newProxyInstance(ClassLoader.getSystemClassLoader(),
//                        new Class[]{CountService.class},
//                        new JdkHandler(delegate));
//        return jdkProxy;
//    }
//
//    private static class JdkHandler implements InvocationHandler {
//
//        final Object delegate;
//
//        JdkHandler(Object delegate) {
//            this.delegate = delegate;
//        }
//
//        public Object invoke(Object object, Method method, Object[] objects)
//                throws Throwable {
//            return method.invoke(delegate, objects);
//        }
//    }
//
//    private static CountService createCglibDynamicProxy(
//            final CountService delegate) throws Exception {
//        Enhancer enhancer = new Enhancer();
//        enhancer.setCallback(new CglibInterceptor(delegate));
//        enhancer.setInterfaces(new Class[]{CountService.class});
//        CountService cglibProxy = (CountService) enhancer.create();
//        return cglibProxy;
//    }
//
//    private static class CglibInterceptor implements MethodInterceptor {
//
//        final Object delegate;
//
//        CglibInterceptor(Object delegate) {
//            this.delegate = delegate;
//        }
//
//        public Object intercept(Object object, Method method, Object[] objects,
//                                MethodProxy methodProxy) throws Throwable {
//            return methodProxy.invoke(delegate, objects);
//        }
//    }
//}
