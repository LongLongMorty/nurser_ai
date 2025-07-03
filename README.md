#### deepseek秘钥设置
src/main/resources/application.yml 里边设置
#### redis在哪里使用了
我在系统里面加了个ai推荐食谱功能，然后我把采纳的ai意见存在一个表里面，并且用这个表的内容开发基于这个历史采纳表的推荐功能，我用redis存这些经常被我推荐功能所使用的历史采纳表里面的内容，推荐功能会频繁读取历史采纳的数据，Redis 能极大提升读取速度，定期或实时同步历史采纳表的数据到 Redis，推荐时优先从 Redis 读取
只有当 Redis 没有数据时，再去数据库查询，并回写到 Redis。
#### 运行还要做什么
首先得加秘钥进去，然后运行ai_recommendation_simple_v4_final.sql，加几个表到数据库里面，注意看看AI_RECOMMENDATION_README.md以及DOCKER_DEPLOYMENT.md，讲解了ai部分以及docker部署部分，正常不要去docker部署，不要去运行那些文件，只是提供了一个快速部署的方法，不会用docker的别乱来

