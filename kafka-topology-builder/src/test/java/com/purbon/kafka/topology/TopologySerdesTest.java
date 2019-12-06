package com.purbon.kafka.topology;

import com.purbon.kafka.topology.model.Project;
import com.purbon.kafka.topology.model.TopicImpl;
import com.purbon.kafka.topology.model.Topology;
import com.purbon.kafka.topology.model.users.Connector;
import com.purbon.kafka.topology.model.users.Consumer;
import com.purbon.kafka.topology.model.users.KStream;
import com.purbon.kafka.topology.model.users.Producer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TopologySerdesTest {


  private TopologySerdes parser;

  @Before
  public void setup() {
    parser = new TopologySerdes();
  }

  @Test
  public void testTopologySerialisation() throws IOException {

    Topology topology = new Topology();
    topology.setTeam("team");
    topology.setSource("source");
    topology.setProjects(buildProjects());

    String topologyYamlString = parser.serialise(topology);
    Topology deserTopology = parser.deserialise(topologyYamlString);

    Assert.assertEquals(topology.getTeam(), deserTopology.getTeam());
    Assert.assertEquals(topology.getProjects().size(), deserTopology.getProjects().size());
  }

  @Test
  public void testTopicConfigSerdes() throws IOException {

    Topology topology = new Topology();
    topology.setTeam("team");
    topology.setSource("source");

    TopicImpl topic = new TopicImpl();
    topic.setName("foo");
    HashMap<String, String> topicConfig = new HashMap<>();
    topicConfig.put("num.partitions", "3");
    topicConfig.put("replication.factor", "2");
    topic.setConfig(topicConfig);

    TopicImpl topicBar = new TopicImpl();
    topicBar.setName("bar");
    HashMap<String, String> topicBarConfig = new HashMap<>();
    topicBarConfig.put("num.partitions", "3");
    topicBarConfig.put("replication.factor", "2");
    topicBar.setConfig(topicBarConfig);

    Project project = new Project("foo");

    KStream kstreamApp = new KStream();
    kstreamApp.setPrincipal("App0");
    HashMap<String, List<String>> topics = new HashMap<>();
    topics.put(KStream.READ_TOPICS, Arrays.asList("topicA", "topicB"));
    topics.put(KStream.WRITE_TOPICS, Arrays.asList("topicC", "topicD"));
    kstreamApp.setTopics(topics);
    project.setStreams(Collections.singletonList(kstreamApp));

    Connector connector1 = new Connector();
    connector1.setPrincipal("Connect1");
    HashMap<String, List<String>> topics1 = new HashMap<>();
    topics1.put(KStream.READ_TOPICS, Arrays.asList("topicA", "topicB"));
    connector1.setTopics(topics1);

    Connector connector2 = new Connector();
    connector2.setPrincipal("Connect2");
    HashMap<String, List<String>> topics2 = new HashMap<>();
    topics2.put(KStream.WRITE_TOPICS, Arrays.asList("topicC", "topicD"));
    connector2.setTopics(topics2);
    project.setConnectors(Arrays.asList(connector1, connector2));

    Consumer consumer0 = new Consumer("app0");
    Consumer consumer1 = new Consumer("app1");
    project.setConsumers(Arrays.asList(consumer0, consumer1));

    project.setTopics(Arrays.asList(topic, topicBar));

    Project project2 = new Project("bar");
    project2.setTopics(Arrays.asList(topicBar));

    topology.setProjects(Arrays.asList(project, project2));

    String topologyYamlString = parser.serialise(topology);

    Topology deserTopology = parser.deserialise(topologyYamlString);

    Project serdesProject = deserTopology.getProjects().get(0);
    TopicImpl serdesTopic = serdesProject.getTopics().get(0);

    Assert.assertEquals(topic.getName(), serdesTopic.getName());
    Assert.assertEquals(topic.getConfig().get("num.partitions"), serdesTopic.getConfig().get("num.partitions"));

  }



  private List<Project> buildProjects() {

    Project project = new Project();
    project.setName("project");
    project.setConsumers(buildConsumers());
    project.setProducers(buildProducers());
    project.setStreams(buildStreams());

    return Collections.singletonList(project);
  }

  private List<KStream> buildStreams() {
    List<KStream> streams = new ArrayList<KStream>();
    HashMap<String, List<String>> topics = new HashMap<String, List<String>>();
    topics.put("read", Arrays.asList("topic1", "topic3"));
    topics.put("write", Arrays.asList("topic2", "topic4"));
    streams.add(new KStream("app3", topics));

    topics = new HashMap<String, List<String>>();
    topics.put("read", Arrays.asList("topic2", "topic4"));
    topics.put("write", Arrays.asList("topic5"));
    streams.add(new KStream("app4", topics));

    return streams;
  }

  private List<Producer> buildProducers() {
    List<Producer> producers = new ArrayList<Producer>();
    producers.add(new Producer("app1"));
    producers.add(new Producer("app3"));
    return producers;
  }

  private List<Consumer> buildConsumers() {
    List<Consumer> consumers = new ArrayList<Consumer>();
    consumers.add(new Consumer("app1"));
    consumers.add(new Consumer("app2"));
    return consumers;
  }
}