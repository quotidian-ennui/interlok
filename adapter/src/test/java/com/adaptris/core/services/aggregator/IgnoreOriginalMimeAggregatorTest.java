package com.adaptris.core.services.aggregator;

import java.util.concurrent.TimeUnit;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.NullService;
import com.adaptris.core.services.LogMessageService;
import com.adaptris.core.services.aggregator.IgnoreOriginalMimeAggregator;
import com.adaptris.core.services.aggregator.MimeAggregator;
import com.adaptris.core.services.mime.MimeJunitHelper;
import com.adaptris.core.services.splitter.LineCountSplitter;
import com.adaptris.core.services.splitter.MimePartSplitter;
import com.adaptris.core.services.splitter.SplitJoinService;
import com.adaptris.core.services.splitter.SplitJoinServiceTest;
import com.adaptris.core.services.splitter.SplitterCase;
import com.adaptris.core.util.MimeHelper;
import com.adaptris.util.TimeInterval;
import com.adaptris.util.text.mime.MultiPartInput;

public class IgnoreOriginalMimeAggregatorTest extends MimeAggregatorCase {

  public IgnoreOriginalMimeAggregatorTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
  }

  @Override
  protected void tearDown() throws Exception {
  }

  public void testService_ContentEncoding() throws Exception {
    // This is a 100 line message, so we expect to get 10 parts.
    AdaptrisMessage msg = SplitterCase.createLineCountMessageInput();
    SplitJoinService service = new SplitJoinService();
    // The service doesn't actually matter right now.
    service.setService(new NullService());
    service.setTimeout(new TimeInterval(10L, TimeUnit.SECONDS));
    service.setSplitter(new LineCountSplitter());
    MimeAggregator aggr = createAggregatorForTests();
    aggr.setEncoding("base64");
    service.setAggregator(aggr);
    execute(service, msg);
    MultiPartInput input = MimeHelper.create(msg, false);
    assertEquals(10, input.size());
  }

  public void testService_ContentIdProvided() throws Exception {
    // This is a 100 line message, so we expect to get 10 parts.
    AdaptrisMessage msg = SplitterCase.createLineCountMessageInput();
    SplitJoinService service = new SplitJoinService();
    // The service doesn't actually matter right now.
    service.setService(createAddMetadataService(getName()));
    service.setTimeout(new TimeInterval(10L, TimeUnit.SECONDS));
    service.setSplitter(new LineCountSplitter());
    MimeAggregator aggr = createAggregatorForTests();
    aggr.setPartContentIdMetadataKey(getName());
    service.setAggregator(aggr);
    execute(service, msg);
    MultiPartInput input = MimeHelper.create(msg, false);
    assertEquals(10, input.size());
  }

  public void testService_MimeSplitter() throws Exception {
    // This is a 3 part message, so that should generate 3 split messages; which should generate 3 parts at the end.
    AdaptrisMessage msg = MimeJunitHelper.create();
    SplitJoinService service = new SplitJoinService();
    // The service doesn't actually matter right now.
    service.setService(new NullService());
    service.setTimeout(new TimeInterval(10L, TimeUnit.SECONDS));
    service.setSplitter(new MimePartSplitter());
    MimeAggregator aggr = createAggregatorForTests();
    aggr.setEncoding("base64");
    service.setAggregator(aggr);
    execute(service, msg);
    MultiPartInput input = MimeHelper.create(msg, false);
    assertEquals(3, input.size());
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    SplitJoinService service = new SplitJoinService();
    service.setService(SplitJoinServiceTest.wrap(new LogMessageService(), new NullService()));
    service.setSplitter(new MimePartSplitter());
    service.setAggregator(new IgnoreOriginalMimeAggregator());
    return service;
  }

  @Override
  protected IgnoreOriginalMimeAggregator createAggregatorForTests() {
    return new IgnoreOriginalMimeAggregator();
  }

  @Override
  protected String getExampleCommentHeader(Object o) {
    return super.getExampleCommentHeader(o) + "\n<!--"
        + "\nConsider the following Multipart Message; (parts are illustrative and may not actually be RFC2045 etc compliant)"
        + "\nContent-ID : 12345" + "\n---BOUNDARY" + "\nContent-Id : abcde" + "\n\nThe quick brown fox jumps over the lazy dog"
        + "\n---BOUNDARY--" + "\nContent-ID : defg" + "\n\nThe five boxing wizards jump quickly" + "\n---BOUNDARY--"
        + "\n\nUsing the MimeSplitter this would generate you 2 split messages; each of which would go through"
        + "\nthe configured service list with the resulting output" + "\nMimeVersion: 1.0" + "\nContent-ID: <msg unique-id>" + "\n"
        + "\n---AnotherBoundary" + "\nContent-ID: abcde" + "\n\nThe quick brown fox jumps over the lazy dog"
        + "\n--AnotherBoundary--" + "\nContent-Id : defg" + "\n\nThe five boxing wizards jump quickly" + "\n--AnotherBoundary--"
        + "\n\n" + "\ni.e. the original message ignored." + "\n-->\n";
  }

  @Override
  protected String createBaseFileName(Object object) {
    return super.createBaseFileName(object) + "-IgnoreOriginalMimeAggregator";
  }
}
