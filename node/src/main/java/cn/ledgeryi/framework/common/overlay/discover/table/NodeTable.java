package cn.ledgeryi.framework.common.overlay.discover.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import cn.ledgeryi.framework.common.overlay.discover.node.Node;

@Slf4j(topic = "discover")
public class NodeTable {

  private final Node node;  // home node
  private transient NodeBucket[] buckets;
  private transient List<NodeEntry> nodes;

  public NodeTable(Node n) {
    this.node = n;
    initialize();
  }

  public Node getNode() {
    return node;
  }

  public final void initialize() {
    nodes = new ArrayList<>();
    //256个列表
    buckets = new NodeBucket[KademliaOptions.BINS];
    for (int i = 0; i < KademliaOptions.BINS; i++) {
      buckets[i] = new NodeBucket(i);
    }
  }

  public synchronized Node addNode(Node n) {
    NodeEntry e = new NodeEntry(node.getId(), n);
    if (nodes.contains(e)) {
      nodes.forEach(nodeEntry -> {
        if (nodeEntry.equals(e)) {
          nodeEntry.touch();
        }
      });
      return null;
    }
    NodeEntry lastSeen = buckets[getBucketId(e)].addNode(e);
    if (lastSeen != null) {
      return lastSeen.getNode();
    }
    if (!nodes.contains(e)) {
      nodes.add(e);
    }
    return null;
  }

  public synchronized void dropNode(Node n) {
    NodeEntry e = new NodeEntry(node.getId(), n);
    buckets[getBucketId(e)].dropNode(e);
    nodes.remove(e);
  }

  public synchronized boolean contains(Node n) {
    NodeEntry e = new NodeEntry(node.getId(), n);
    for (NodeBucket b : buckets) {
      if (b.getNodes().contains(e)) {
        return true;
      }
    }
    return false;
  }

  public synchronized void touchNode(Node n) {
    NodeEntry e = new NodeEntry(node.getId(), n);
    for (NodeBucket b : buckets) {
      if (b.getNodes().contains(e)) {
        b.getNodes().get(b.getNodes().indexOf(e)).touch();
        break;
      }
    }
  }

  public int getBucketsCount() {
    int i = 0;
    for (NodeBucket b : buckets) {
      if (b.getNodesCount() > 0) {
        i++;
      }
    }
    return i;
  }

  public synchronized NodeBucket[] getBuckets() {
    return buckets;
  }

  public int getBucketId(NodeEntry e) {
    int id = e.getDistance() - 1;
    return id < 0 ? 0 : id;
  }

  public synchronized int getNodesCount() {
    return nodes.size();
  }

  public synchronized List<NodeEntry> getAllNodes() {
    List<NodeEntry> nodes = new ArrayList<>();

    for (NodeBucket b : buckets) {
      for (NodeEntry e : b.getNodes()) {
        if (!e.getNode().equals(node)) {
          nodes.add(e);
        }
      }
    }

    return nodes;
  }

  public synchronized List<Node> getClosestNodes(byte[] targetId) {
    List<NodeEntry> closestEntries = getAllNodes();
    List<Node> closestNodes = new ArrayList<>();
    Collections.sort(closestEntries, new DistanceComparator(targetId));
    if (closestEntries.size() > KademliaOptions.BUCKET_SIZE) {
      closestEntries = closestEntries.subList(0, KademliaOptions.BUCKET_SIZE);
    }
    for (NodeEntry e : closestEntries) {
      if (!e.getNode().isDiscoveryNode()) {
        closestNodes.add(e.getNode());
      }
    }
    return closestNodes;
  }
}