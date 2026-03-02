const base = {
    get() {
        return {
            url : "/springboot/",
            name: "springboot",
            // 退出到首页链接
            indexUrl: '/'
            // url : "http://localhost:8081/springboot/",
            // name: "springboot",
            // // 退出到首页链接
            // indexUrl: 'http://localhost:8081/springboot/front/index.html'
        };
    },
    getProjectName(){
        return {
            projectName: "员工绩效考核管理系统"
        } 
    }
}
export default base
