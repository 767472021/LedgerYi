## 节点发现启动
节点启动之后想要加入Kademlia网络，首先需要进行节点发现，通常有如下的步骤：
- 新节点A需要一个种子节点B作为引导，并把该种子节点加入到K-Bucket中。
- 生成一个随机的节点ID，直到离开网络一直使用。
- 向节点B发送FindNode消息。
- 节点B在收到节点A的FindNode消息后，会根据协议的约定找到K个距离A最近的节点，向A节点回复包含了这K个节点的Neighbours消息。
- A收到Neighbours消息中的节点以后，就把它们加入到自己的K-Bucket中。
- 然后节点A会继续向这些刚拿到节点发起FindNode消息，如此往复，直到A建立了足够详细的路由表。

## 节点K桶（K-Bucket）的刷新
由于P2P网络中的节点可能随时下线或者由于网络环境的变化而变得不可达，实际上这种情况通常频繁发生，
为了保证有足够的有效节点能够进行连接，同时避免节点长时间连接同一批节点，因此需要周期性地运行节点发现协议将节点的K-Bucket进行刷新，
以补充更多的新节点，替换掉一些不可达的旧节点。

K-Bucket主要有三种方式来刷新路由表：
- 主动收集节点，主动发起FindNode消息查询节点，从而更新K-Bucket的节点信息。
- 被动收集节点，当收到其他节点发送过来FindNode消息，会把对方的节点Node ID加入到某个K-Bucket中。
- 检测失效节点，周期性的发起PING请求，判断K-Bucket中某个节点是否在线，然后清理K-Bucket中那些下线的节点。

当一个Node ID被用来更新K-Bucket的时候进行如下步骤：
- 计算自己和目标Node ID的距离d。
- 通过距离d找到对应的K-Bucket，如果Node ID已经在K-Bucket中了，则把这个节点移到K-Bucket的末尾。
- 如果不在K-Bucket中则有两种情况：
  - 1.如果该K-Bucket存储的节点小于K个，则直接把目标节点插入到K-Bucket尾部；
  - 2.如果该K-Bucket存储节点大于等于K个，则选择K-桶中的头部节点进行PING操作，检测节点是否存活。如果头部节点没有响应，则移除该头部节点，
      并将目标节点插入到队列尾部；如果头部节点有响应，则把头部节点移到队列尾部，同时忽略目标节点。

通过这种更新策略可以保证在线时间长的节点有较大的可能继续保存在K-Bucket中，提高了稳定网络构建路由表的成本。

## Kademlia算法
Kademlia是DHT（分布式哈希表）的许多版本之一。与之前版本的DHT相比，Kademlia具有很多优势，如：
- 它减少了节点沟通时所需消息的数量
- 用并行和异步查询来减少坏节点造成的延迟
简言之，它提高了通信的效率。

Kademlia网络的特征在于三个常数：ALPHA、BUCKET_SIZE、节点ID的长度（64位）

## Kademlia参数
Kademlia算法在执行过程中，有以下几个重要参数(KademliaOptions.java)可以进行调整：
```
ALPHA            = 3
MAX_STEPS        = 8
BUCKET_REFRESH   = 7200
BUCKET_SIZE      = 16
BUCKET_COUNT     = 256
```
- ALPHA：查询并发数，即每次同时向 ALPHA 个邻居节点发送FindNeighbours消息。
- MAX_STEPS：最大并发查询数，即运行一次邻居发现协议，最多会向 ALPHA * MAX_STEPS个不同邻居节点发送FindNeighbours消息。
- BUCKET_REFRESH：运行邻居发现协议的时间间隔。
- BUCKET_SIZE：每个BUCKET桶最多保存的节点数，即桶的容量。
- BUCKET_COUNTBUCKET：桶的数量。

这些参数将对Kademlia算法运行的网络规模以及节点K-Bucket的数量进行影响，节点可以根据实际情况进行调整。

## 节点
为什么会有非真实存在的节点？这个字段的意义是什么？

原因是：Kademlia的网络可由一棵二叉树来表示，树高即为节点id的长度，每一个叶子对应一个Kademlia节点。由此可知，当节点ID的长度==512时，
这棵树最多可以容纳2^511个Kademlia节点。在实际情况中，不可能存在数量如此庞大的节点。
为了找寻其他节点，会用同样的算法生成一个符合规则的id，这个id可能没有对应真实的节点，但无论是否真实存在，它都将作为扩充路由表的重要辅助。

## 节点发现协议
P2P网络中节点的发现通过执行Kademlia算法，节点按照协议流程不断地向他已知的节点询问其邻居节点信息，并将收到的节点信息存储到自己的K桶之中，
在经过有限次的迭代之后完成一次节点发现的过程。

## Kademlia 算法中的通信消息
Kademlia算法实现中，节点之间的通信是基于 UDP 协议的，基于UDP协议设置了 4 个主要的通信消息：
- Ping消息：用于探测一个节点是否在线。
- Pong消息：用于响应 Ping 命令。
- FindNode消息：用于查找与目标节点（Target Node）异或距离最近的其他节点。
- Neighbours消息：用于响应FindNode命令，会返回一个或多个节点。

以上4种消息类型，两两成对，Pong消息是对Ping消息的应答，用于节点之间的保活；
Neighbours消息是对FindNode消息的应答，用于节点之间交换邻居节点的信息。
节点经过与其他节点不断的消息通信，最终将自己发现的节点存储到相应的桶里面，从而完善自己的网络拓扑结构。

## 节点状态
协议消息和节点状态被用来解决节点状态会动态变化的问题。在节点发现的过程中，节点共有如下几种状态：
- discovered：所有节点的初始状态。本地节点会向所有discovered的节点发生ping。
- alive：从其收到正确的pong的节点，即节点在线。
- dead：未收到pong，即节点不在线。
- active：节点在活动状态，并且在路由表中。
- evictcandidate：原本存在于路由表中，被尝试加入的新节点所被挑战的节点。
- nonactive：挑战失败，被移除路由表表的节点。

## 路由表更新
路由表的更新有两种操作：直接加入新节点，或用新节点替换。替换的目的是为了保证路由表中节点的活跃度。
每个节点利用协议消息和其他节点沟通，并通过接收到的反馈来改变节点的状态。

路由表更新的过程如下：
- 读取预设的种子节点，向其发送ping消息，若在线(收到回应的pong消息)，则将其加入路由表，并把状态置为active
- 向路由表中的节点请求邻居
- 向新发现的节点发送ping消息，如收到pong消息，表明该节点在线，将其置为active，否则置为dead
- 尝试将alive的节点根据其到自身的距离加入路由表（相应的k桶中）
  - 若桶未满，则直接加入，变为active
  - 若桶已满，则找出modified值最小的节点（modified是上一次该节点ping通的时间，modified值越小，活跃几率越小），对其发起挑战，并将被挑战的节点状态从active变为evictcandidate
- 向evictcandidate节点发送ping消息
  - 从evictcandidate节点收到pong消息，该节点在线，重新置其为active，修改其modified值为当前时间；alive的节点不变化
  - 未从evictcandidate节点收到pong消息，该节点离线，将其置为nonactive并移出路由表；alive的节点加入路由表，置为active
  
由以上过程可知，仅当路由表中节点离线时，新节点才会加入。

## 连接建立
每个节点能够建立连接的数目是有限的。打分机制对路由表中的节点进行评价并按照其总分排序，从众多节点中选出最优质的节点。
此处的“优质”是从多个角度来进行评价的，不仅需要考虑与其连接的稳定性，还需要考虑对方保存的信息是否有价值。

### 节点打分
节点打分是为了找出相对稳定的节点并与其建立连接，打分越高则节点越稳定。节点打分有如下维度：
- 丢包率：收到的pong的消息数目/发出的ping消息数目
- 网络延迟：收到pong消息时间-发出ping消息时间
- tcp流量：节点之间的活跃度
- 节点连接稳定性：节点连接断开次数
- handshake：是否曾经握手成功
- 处罚：处于处罚状态的节点得分清零，不考虑其他维度

计算节点分数时将优先考虑处罚状态，节点将因为如下原因受到处罚：
- 节点距上次断开连接时间不到60s
- 节点P2P版本，端口等信息不一致
- 区块信息不一致（如创世块，固化块不同）

handshake并非tcp的三次握手，而是节点之间连接建立成功后对于自己和对方是否处于同一条链上的确认。

### 节点过滤
节点过滤主要考虑待连接节点的可靠程度，如：连接是否稳定，是否为恶意节点等。

在挑选节点进行连接时，会首先考虑配置文件中预先声明的节点：
- active nodes：跳过打分规则，主动向其建立连接
- passive nodes：跳过打分规则，接收从对方发来的连接请求

这两种节点都可称为可信节点。需要注意的是打分规则只是过滤规则中的一条，在连接时，可信节点会被优先考虑并跳过打分，
但仍需要通过其他过滤规则才能够成功连接。

节点过滤的流程如下：
- 判断将连接的节点是否自身
- 判断本地连接是否达到最大值（可在配置文件中进行指定）
- 是否已经与对方建立连接
- 来自同一ip的连接是否达到最大值，包括发送往同一ip的连接。这个判断是为了防御攻击
- 用打分规则，选择分数较高的节点

在被动接受来自其他节点的连接时，无需考虑连接是否来自于自己。如果收到相同请求，则会保持原有连接，丢弃当前新建连接。

### 过滤规则与日蚀攻击（eclipse attack）
日蚀攻击是面向P2P网络的一种攻击类型。节点可以在同一时间保持的TCP连接数是固定的，若可用的连接数被攻击者占满，受害者将无法从正常的节点接收信息，
而只能接收攻击者发来的（通常是恶意的）信息，从而控制受害者对信息的访问。

对于区块链上的节点来说，当某个节点收到大量的虚假信息，它就会失去作用。如果这样的受害者节点过多，会影响整个区块链网络的正常运行。

具体实现中已经考虑到了这种情况，并进行了一定的应对措施：
- 在最大连接数确定的情况下，必须有部分连接是由自身节点主动发起的连接（而非全部被动接受）
- 来自同一ip的连接数最多为2

在同一台机器上启动多个节点的行为将被视为攻击行为。基于此原因，来自同一ip的最大连接数会受到限制。
同时，节点启动时会优先连接预设的可信节点（分为active和passive nodes，主动向active node发起连接，优先接受passive node发来的连接）。
通过对节点的过滤，提高了攻击者进行攻击的难度。

## handshake
handshake发生在已经与其他节点建立tcp连接后，开始同步之前，目的是为了确认区块信息是否一致。若区块信息不一致，就会失去与其进行交互的价值。

handshake的双方会互相发送HelloMessage，该信息包含如下内容：
- 节点基本信息
- 当前时间
- 创世块id
- 固化块id
- 头块id

进行连接的双方会验证HelloMessage，在HelloMessage的验证中，有如下情况连接会被断开：
- P2P版本不同
- 创世块不一致
- 若本地固化块高度大于对方固化块高度，且对方的固化块不在本地保存的链中

handshake成功后，节点连接成功。
