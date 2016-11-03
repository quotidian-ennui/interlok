package com.adaptris.tester.runtime;

import com.adaptris.tester.report.junit.JUnitReportTestResults;
import com.adaptris.tester.runtime.clients.TestClient;
import com.adaptris.tester.runtime.helpers.Helper;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.isEmpty;

@XStreamAlias("service-test")
public class ServiceTest implements TestComponent {

  private String uniqueId;

  private TestClient testClient;
  private List<Helper> helpers;
  @XStreamImplicit
  private List<TestList> testLists;

  public ServiceTest(){
    setHelpers(new ArrayList<Helper>());
    setTestLists(new ArrayList<TestList>());
  }

  public void setUniqueId(String uniqueId) {
    if (isEmpty(uniqueId)) {
      throw new IllegalArgumentException();
    }
    this.uniqueId = uniqueId;
  }

  @Override
  public String getUniqueId() {
    return uniqueId;
  }

  public void setTestClient(TestClient testClient) {
    this.testClient = testClient;
  }

  public TestClient getTestClient() {
    return testClient;
  }

  public void setHelpers(List<Helper> helpers) {
    this.helpers = helpers;
  }

  public List<Helper> getHelpers() {
    return helpers;
  }

  private void initHelpers() throws ServiceTestException {
    for(Helper helper : getHelpers()){
      helper.init();
    }
  }

  public void closeHelpers()  {
    for(Helper helper : getHelpers()){
      IOUtils.closeQuietly(helper);
    }
  }

  public Map<String, String> getHelperProperties(){
    Map<String,String> p = new HashMap<>();
    for(Helper helper : getHelpers()){
      p.putAll(helper.getHelperProperties());
    }
    return p;
  }

  public void setTestLists(List<TestList> adapterTestLists) {
    this.testLists = adapterTestLists;
  }

  public List<TestList> getTestLists() {
    return testLists;
  }

  public void addTestList(TestList adapterTestList){
    this.testLists.add(adapterTestList);
  }

  public JUnitReportTestResults execute() throws ServiceTestException {
    initHelpers();
    testClient.init();
    try {
      JUnitReportTestResults results = new JUnitReportTestResults(uniqueId);
      for(TestList tests : getTestLists()){
        results.addTestSuites(tests.execute(testClient, getHelperProperties()));
      }
      return results;
    } finally {
      IOUtils.closeQuietly(testClient);
      closeHelpers();
    }
  }
}