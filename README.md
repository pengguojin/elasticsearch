# elasticsearch

## 项目介绍
elasticsearch学习工程，涉及到java-API有TransportClient、RestClient、springboot，jest、elasticsearch-sql(非官方)			
项目基于springboot+maven构建

## 教程

#### 一、TransportClient
- 1、官方文档
[官方文档地址](https://www.elastic.co/guide/en/elasticsearch/client/java-api/current/transport-client.html)

- 2、引入maven依赖
```xml
<dependency>
	<groupId>org.elasticsearch.client</groupId>
	<artifactId>transport</artifactId>
</dependency>
```
- 3、新建一个配置类，配置client		
> 其中esClusterName是集群名称；esHost是ES的访问地址IP，如localhost；esPort是ES端口，如果是集群，则增加多个addTransportAddress

```java
@Configuration
public class ESConfig {
	@Bean
	public TransportClient client() throws UnknownHostException {
		TransportClient client = new PreBuiltTransportClient(
				Settings.builder().put("cluster.name", esClusterName).build());
		client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(esHost), esPort));
		return client;
	}
}
```

- 4、使用
> 直接通过spring的依赖注入的方式即可，如下
```java
@Autowired
private TransportClient client;
```

#### 二、RestClient
> RestClient只支持elasticsearch5.0以上的版本，是TransportClient的替代品，TransportClient有性能问题，高版本建议使用RestClient
- 1、官方文档
[官方文档地址](https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/index.html)
- 2、引入maven依赖
> RestClient包含low level版本和high level版本，low版本功能很弱，high版本功能比较多，high基于low版本，使用high需要引入low版本，如下
```xml
<!-- low level -->
<dependency>
	<groupId>org.elasticsearch.client</groupId>
	<artifactId>elasticsearch-rest-client</artifactId>
	<version>5.6.5</version>
</dependency>
<!-- high level -->
<dependency>
	<groupId>org.elasticsearch.client</groupId>
	<artifactId>elasticsearch-rest-high-level-client</artifactId>
	<version>5.6.5</version>
</dependency>
```
- 3、基于springboot2.1之前版本的配置
	- 基于springboot2.1之前版本，需要手动添加配置

	- 在application.yml配置文件添加自定义配置
		```
		elasticsearch:
			ip: localhost:9200,
		```
	- 新建一个配置类
		```java
		@Configuration
		public class ElasticsearchRestClient {
			@Value("${elasticsearch.ip}")
			String[] ipAddress;
			
			private static final int ADDRESS_LENGTH = 2;

			private static final String HTTP_SCHEME = "http";

			// 先配置low版本
			@Bean
			public RestClient restClient() {
				HttpHost[] hosts = Arrays.stream(ipAddress).map(this::makeHttpHost).filter(Objects::nonNull)
						.toArray(HttpHost[]::new);
				log.debug("hosts:{}", Arrays.toString(hosts));
				return RestClient.builder(hosts).build();
			}

			// 通过low版本依赖注入，配置high版本
			@Bean(name = "highLevelClient")
			public RestHighLevelClient highLevelClient(@Autowired RestClient restClient) {
				return new RestHighLevelClient(restClient);
			}
			
			// 分隔配置多个集群IP
			private HttpHost makeHttpHost(String s) {
				String[] address = s.split(":");
				if (address.length == ADDRESS_LENGTH) {
					String ip = address[0];
					int port = Integer.parseInt(address[1]);
					return new HttpHost(ip, port, HTTP_SCHEME);
				} else {
					return null;
				}
			}
		}
		```

- 4、基于springboot2.1后版本的配置
	- 基于springboot2.1后版本就简单得多了，springboot2.1后增加了restclient的配置
	- 直接在application.yml配置文件添加配置即可
		```yml
		spring:
		  elasticsearch:
			rest:
			  uris: http://localhost:9200,http://localhost:8300
		```
	- 如果使用low版本，直接使用@Autowired注入RestClient即可
	- 如果使用high版本，添加配置类，然后是@Autowired注入RestHighLevelClient即可
		```java
		@Configuration
		public class ElasticsearchRestClient {
			@Bean(name = "highLevelClient")
			public RestHighLevelClient highLevelClient(@Autowired RestClient restClient) {
				return new RestHighLevelClient(restClient);
			}
		}
		```
		
#### 三、springboot
> springboot-elasticsearch是基于springdata的
- 1、官方文档
[官方文档地址](https://docs.spring.io/spring-data/elasticsearch/docs/3.1.0.RELEASE/reference/html)
- 2、引入maven依赖
	```
	<dependency>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-data-elasticsearch</artifactId>
	</dependency>
	```
- 3、在application.yml添加配置，springdata-elasticsearch是有版本限制的，具体参考官方文档
	```
		#节点名字，默认elasticsearch
		spring.data.elasticsearch.cluster-name=jin
		#节点地址，多个节点用逗号隔开
		spring.data.elasticsearch.cluster-nodes=127.0.0.1:9300
		#spring.data.elasticsearch.local=false
		spring.data.elasticsearch.repositories.enable=true
	```
- 4、新建一个实体类
	```java
	// indexName对应es的索引，type对应es的类型
	@Document(indexName = "user", type = "teacher")
	public class UserEntity {
	// 主键ID
	@Id
	private String id;

	@Field
	private Integer age;
	// ik分词
	@Field(searchAnalyzer = "ik_max_word", analyzer = "ik_max_word")
	private String name;

	@Field
	private Integer course;
	// 自定义时间格式
	@Field(format = DateFormat.custom, pattern = "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis")
	private Date create_time;
	// get set 省略
	```
- 5、新建一个配置接口，继承ElasticsearchRepository
	```java
	@Component
	public interface UserRepository
			extends ElasticsearchRepository<UserEntity, String>, PagingAndSortingRepository<UserEntity, String> {
		// 自定义的方法
	}
	```
	
- 6、使用，直接注入UserRepository即可
	```java
	@Autowired
	private UserRepository rep;

	// 保存
	rep.save(user);
	// 删除
	rep.deleteById(id);
	// 分页查询
	Pageable pageable = PageRequest.of(0, 10);
	Page<UserEntity> user = rep.findAll(pageable);
	....
	```
	
#### 四、Jest
- 1、官方文档
[官方文档地址](https://github.com/searchbox-io/Jest)
- 2、引入maven依赖
	```xml
	<!-- 版本跟ES版本一致即可 -->
	<dependency>
		<groupId>io.searchbox</groupId>
		<artifactId>jest</artifactId>
		<version>版本</version>
	</dependency>
	```
- 3、在application.yml添加配置
	```yml
	spring:
	  elasticsearch:
		jest:
		  uris: http://localhost:8200
	```
- 4、使用，直接注入JestClient即可