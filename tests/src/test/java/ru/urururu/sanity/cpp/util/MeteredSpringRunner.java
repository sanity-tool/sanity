package ru.urururu.sanity.cpp.util;

import com.codahale.metrics.Timer;
import org.junit.rules.MethodRule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.manipulation.Sorter;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerScheduler;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.lang.annotation.Annotation;
import java.util.List;

public class MeteredSpringRunner extends SpringJUnit4ClassRunner {

    public MeteredSpringRunner(Class<?> clazz) throws InitializationError {
        super(clazz);
    }

    @Override
    protected TestContextManager createTestContextManager(Class<?> clazz) {
        try (Timer.Context ctx = Metrics.time(getClass(), "createTestContextManager")) {
            return super.createTestContextManager(clazz);
        }
    }

    @Override
    public Description getDescription() {
        try (Timer.Context ctx = Metrics.time(getClass(), "getDescription")) {
            return super.getDescription();
        }
    }

    @Override
    public void run(RunNotifier notifier) {
        try (Timer.Context ctx = Metrics.time(getClass(), "run")) {
            super.run(notifier);
        }
    }

    @Override
    protected Statement withBeforeClasses(Statement statement) {
        try (Timer.Context ctx = Metrics.time(getClass(), "withBeforeClasses")) {
            return super.withBeforeClasses(statement);
        }
    }

    @Override
    protected Statement withAfterClasses(Statement statement) {
        try (Timer.Context ctx = Metrics.time(getClass(), "withAfterClasses")) {
            return super.withAfterClasses(statement);
        }
    }

    @Override
    protected Object createTest() throws Exception {
        try (Timer.Context ctx = Metrics.time(getClass(), "createTest")) {
            return super.createTest();
        }
    }

    @Override
    protected void runChild(FrameworkMethod frameworkMethod, RunNotifier notifier) {
        try (Timer.Context ctx = Metrics.time(getClass(), "runChild")) {
            super.runChild(frameworkMethod, notifier);
        }
    }

    @Override
    protected Statement methodBlock(FrameworkMethod frameworkMethod) {
        try (Timer.Context ctx = Metrics.time(getClass(), "methodBlock")) {
            return super.methodBlock(frameworkMethod);
        }
    }

    @Override
    protected boolean isTestMethodIgnored(FrameworkMethod frameworkMethod) {
        try (Timer.Context ctx = Metrics.time(getClass(), "isTestMethodIgnored")) {
            return super.isTestMethodIgnored(frameworkMethod);
        }
    }

    @Override
    protected Statement possiblyExpectingExceptions(FrameworkMethod frameworkMethod, Object testInstance, Statement next) {
        try (Timer.Context ctx = Metrics.time(getClass(), "possiblyExpectingExceptions")) {
            return super.possiblyExpectingExceptions(frameworkMethod, testInstance, next);
        }
    }

    @Override
    protected Class<? extends Throwable> getExpectedException(FrameworkMethod frameworkMethod) {
        try (Timer.Context ctx = Metrics.time(getClass(), "getExpectedException")) {
            return super.getExpectedException(frameworkMethod);
        }
    }

    @Override
    protected Statement withPotentialTimeout(FrameworkMethod frameworkMethod, Object testInstance, Statement next) {
        try (Timer.Context ctx = Metrics.time(getClass(), "withPotentialTimeout")) {
            return super.withPotentialTimeout(frameworkMethod, testInstance, next);
        }
    }

    @Override
    protected long getJUnitTimeout(FrameworkMethod frameworkMethod) {
        try (Timer.Context ctx = Metrics.time(getClass(), "getJUnitTimeout")) {
            return super.getJUnitTimeout(frameworkMethod);
        }
    }

    @Override
    protected long getSpringTimeout(FrameworkMethod frameworkMethod) {
        try (Timer.Context ctx = Metrics.time(getClass(), "getSpringTimeout")) {
            return super.getSpringTimeout(frameworkMethod);
        }
    }

    @Override
    protected Statement withBefores(FrameworkMethod frameworkMethod, Object testInstance, Statement statement) {
        try (Timer.Context ctx = Metrics.time(getClass(), "withBefores")) {
            return super.withBefores(frameworkMethod, testInstance, statement);
        }
    }

    @Override
    protected Statement withAfters(FrameworkMethod frameworkMethod, Object testInstance, Statement statement) {
        try (Timer.Context ctx = Metrics.time(getClass(), "withAfters")) {
            return super.withAfters(frameworkMethod, testInstance, statement);
        }
    }

    @Override
    protected Statement withPotentialRepeat(FrameworkMethod frameworkMethod, Object testInstance, Statement next) {
        try (Timer.Context ctx = Metrics.time(getClass(), "withPotentialRepeat")) {
            return super.withPotentialRepeat(frameworkMethod, testInstance, next);
        }
    }

    @Override
    protected boolean isIgnored(FrameworkMethod child) {
        try (Timer.Context ctx = Metrics.time(getClass(), "isIgnored")) {
            return super.isIgnored(child);
        }
    }

    @Override
    protected Description describeChild(FrameworkMethod method) {
        try (Timer.Context ctx = Metrics.time(getClass(), "describeChild")) {
            return super.describeChild(method);
        }
    }

    @Override
    protected List<FrameworkMethod> getChildren() {
        try (Timer.Context ctx = Metrics.time(getClass(), "getChildren")) {
            return super.getChildren();
        }
    }

    @Override
    protected List<FrameworkMethod> computeTestMethods() {
        try (Timer.Context ctx = Metrics.time(getClass(), "computeTestMethods")) {
            return super.computeTestMethods();
        }
    }

    @Override
    protected void collectInitializationErrors(List<Throwable> errors) {
        try (Timer.Context ctx = Metrics.time(getClass(), "collectInitializationErrors")) {
            super.collectInitializationErrors(errors);
        }
    }

    @Override
    protected void validateNoNonStaticInnerClass(List<Throwable> errors) {
        super.validateNoNonStaticInnerClass(errors);
    }

    @Override
    protected void validateConstructor(List<Throwable> errors) {
        super.validateConstructor(errors);
    }

    @Override
    protected void validateOnlyOneConstructor(List<Throwable> errors) {
        super.validateOnlyOneConstructor(errors);
    }

    @Override
    protected void validateZeroArgConstructor(List<Throwable> errors) {
        super.validateZeroArgConstructor(errors);
    }

    @Override
    protected void validateInstanceMethods(List<Throwable> errors) {
        super.validateInstanceMethods(errors);
    }

    @Override
    protected void validateFields(List<Throwable> errors) {
        super.validateFields(errors);
    }

    @Override
    protected void validateTestMethods(List<Throwable> errors) {
        super.validateTestMethods(errors);
    }

    @Override
    protected String testName(FrameworkMethod method) {
        return super.testName(method);
    }

    @Override
    protected Statement methodInvoker(FrameworkMethod method, Object test) {
        return super.methodInvoker(method, test);
    }

    @Override
    protected List<MethodRule> rules(Object target) {
        return super.rules(target);
    }

    @Override
    protected List<TestRule> getTestRules(Object target) {
        return super.getTestRules(target);
    }

    @Override
    protected TestClass createTestClass(Class<?> testClass) {
        try (Timer.Context ctx = Metrics.time(getClass(), "createTestClass")) {
            return super.createTestClass(testClass);
        }
    }

    @Override
    protected void validatePublicVoidNoArgMethods(Class<? extends Annotation> annotation, boolean isStatic, List<Throwable> errors) {
        super.validatePublicVoidNoArgMethods(annotation, isStatic, errors);
    }

    @Override
    protected Statement classBlock(RunNotifier notifier) {
        return super.classBlock(notifier);
    }

    @Override
    protected List<TestRule> classRules() {
        return super.classRules();
    }

    @Override
    protected Statement childrenInvoker(RunNotifier notifier) {
        return super.childrenInvoker(notifier);
    }

    @Override
    protected String getName() {
        return super.getName();
    }

    @Override
    protected Annotation[] getRunnerAnnotations() {
        return super.getRunnerAnnotations();
    }

    @Override
    public void filter(Filter filter) throws NoTestsRemainException {
        try (Timer.Context ctx = Metrics.time(getClass(), "filter")) {
            super.filter(filter);
        }
    }

    @Override
    public void sort(Sorter sorter) {
        try (Timer.Context ctx = Metrics.time(getClass(), "sort")) {
            super.sort(sorter);
        }
    }

    @Override
    public void setScheduler(RunnerScheduler scheduler) {
        super.setScheduler(scheduler);
    }

    @Override
    public int testCount() {
        try (Timer.Context ctx = Metrics.time(getClass(), "testCount")) {
            return super.testCount();
        }
    }
}
