package net.minecraft.test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.world.server.ServerWorld;

public class TestRegistry {
   private static final Collection<TestFunctionInfo> testFunctions = Lists.newArrayList();
   private static final Set<String> testClassNames = Sets.newHashSet();
   private static final Map<String, Consumer<ServerWorld>> beforeBatchFunctions = Maps.newHashMap();
   private static final Collection<TestFunctionInfo> lastFailedTests = Sets.newHashSet();

   public static Collection<TestFunctionInfo> getTestFunctionsForClassName(String pClassName) {
      return testFunctions.stream().filter((p_229535_1_) -> {
         return isTestFunctionPartOfClass(p_229535_1_, pClassName);
      }).collect(Collectors.toList());
   }

   public static Collection<TestFunctionInfo> getAllTestFunctions() {
      return testFunctions;
   }

   public static Collection<String> getAllTestClassNames() {
      return testClassNames;
   }

   public static boolean isTestClass(String pClassName) {
      return testClassNames.contains(pClassName);
   }

   @Nullable
   public static Consumer<ServerWorld> getBeforeBatchFunction(String pFunctionName) {
      return beforeBatchFunctions.get(pFunctionName);
   }

   public static Optional<TestFunctionInfo> findTestFunction(String pTestName) {
      return getAllTestFunctions().stream().filter((p_229531_1_) -> {
         return p_229531_1_.getTestName().equalsIgnoreCase(pTestName);
      }).findFirst();
   }

   public static TestFunctionInfo getTestFunction(String pTestName) {
      Optional<TestFunctionInfo> optional = findTestFunction(pTestName);
      if (!optional.isPresent()) {
         throw new IllegalArgumentException("Can't find the test function for " + pTestName);
      } else {
         return optional.get();
      }
   }

   private static boolean isTestFunctionPartOfClass(TestFunctionInfo pTestFunction, String pClassName) {
      return pTestFunction.getTestName().toLowerCase().startsWith(pClassName.toLowerCase() + ".");
   }

   public static Collection<TestFunctionInfo> getLastFailedTests() {
      return lastFailedTests;
   }

   public static void rememberFailedTest(TestFunctionInfo pTestFunction) {
      lastFailedTests.add(pTestFunction);
   }

   public static void forgetFailedTests() {
      lastFailedTests.clear();
   }
}