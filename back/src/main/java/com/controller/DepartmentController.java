package com.controller;

import java.text.SimpleDateFormat;
import java.util.*;
import javax.servlet.http.HttpServletRequest;

import com.entity.DepartmentEntity;
import com.entity.vo.DepartmentVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;

import com.entity.view.DepartmentView;

import com.service.DepartmentService;
import com.utils.PageUtils;
import com.utils.R;
import com.utils.MPUtil;


/**
 * 部门
 * 后端接口
 * @author 
 * @email 
 * @date 2021-05-07 10:42:31
 */
@RestController
@RequestMapping("/department")
public class DepartmentController {
    @Autowired
    private DepartmentService departmentService;
    


    /**
     * 后端列表
     */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, DepartmentEntity dname,
                  HttpServletRequest request){
        EntityWrapper<DepartmentEntity> ew = new EntityWrapper<DepartmentEntity>();
		PageUtils page = departmentService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, dname), params), params));

        return R.ok().put("data", page);
    }

    /**
     * 前端列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params, DepartmentEntity dname,
                  HttpServletRequest request){

        EntityWrapper<DepartmentEntity> ew = new EntityWrapper<DepartmentEntity>();
        // 使用 selectListVO 方法返回 DepartmentVO 列表
        List<DepartmentVO> departmentList = departmentService.selectListVO(ew);

        // 手动分页逻辑
        int page = Integer.parseInt(params.getOrDefault("page", "1").toString());
        int size = Integer.parseInt(params.getOrDefault("size", "10").toString());
        int total = departmentList.size();

        // 计算分页
        int fromIndex = (page - 1) * size;
        int toIndex = Math.min(fromIndex + size, total);
        List<DepartmentVO> pageList = departmentList.subList(fromIndex, toIndex);

        // 构建分页响应
        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("pageSize", size);
        result.put("totalPage", (int) Math.ceil((double) total / size));
        result.put("currPage", page);
        result.put("list", pageList);

        return R.ok().put("data", result);
    }

	/**
     * 列表
     */
    @RequestMapping("/lists")
    public R list( DepartmentEntity dname){
       	EntityWrapper<DepartmentEntity> ew = new EntityWrapper<DepartmentEntity>();
      	ew.allEq(MPUtil.allEQMapPre( dname, "dname"));
        return R.ok().put("data", departmentService.selectListView(ew));
    }

	 /**
     * 查询
     */
    @RequestMapping("/query")
    public R query(DepartmentEntity dname){
        EntityWrapper< DepartmentEntity> ew = new EntityWrapper< DepartmentEntity>();
 		ew.allEq(MPUtil.allEQMapPre( dname, "dname"));
		DepartmentView departmentView =  departmentService.selectView(ew);
		return R.ok("查询部门成功").put("data", departmentView);
    }
	
    /**
     * 后端详情
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
        DepartmentEntity dname = departmentService.selectById(id);
        return R.ok().put("data", dname);
    }

    /**
     * 前端详情
     */
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id){
        DepartmentEntity dname = departmentService.selectById(id);
        return R.ok().put("data", dname);
    }
    



    /**
     * 后端保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody DepartmentEntity dname, HttpServletRequest request){
        dname.setId(new Date().getTime() + new Double(Math.floor(Math.random() * 1000)).longValue());
        dname.setCreateTime(new Date());
        departmentService.insert(dname);
        return R.ok();
    }
    
    /**
     * 前端保存
     */
    @RequestMapping("/add")
    public R add(@RequestBody DepartmentEntity dname, HttpServletRequest request){
        dname.setId(new Date().getTime()+new Double(Math.floor(Math.random()*1000)).longValue());
    	//ValidatorUtils.validateEntity(bumen);
        departmentService.insert(dname);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody DepartmentEntity dname, HttpServletRequest request){
        //ValidatorUtils.validateEntity(bumen);
        departmentService.updateById(dname);//全部更新
        return R.ok();
    }

    /**
     * 删除
     */
    /**
     * 删除
     */
    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
        try {
            if (ids == null || ids.length == 0) {
                return R.error("请选择要删除的部门");
            }

            // 🔥 直接删除，数据库会自动处理员工的外键
            boolean result = departmentService.deleteBatchByIds(Arrays.asList(ids));
            if (result) {
                return R.ok("删除成功");
            } else {
                return R.error("删除失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return R.error("删除过程中发生错误: " + e.getMessage());
        }
    }

    /**
     * 提醒接口
     */
	@RequestMapping("/remind/{columnName}/{type}")
	public R remindCount(@PathVariable("columnName") String columnName, HttpServletRequest request, 
						 @PathVariable("type") String type,@RequestParam Map<String, Object> map) {
		map.put("column", columnName);
		map.put("type", type);
		
		if(type.equals("2")) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Calendar c = Calendar.getInstance();
			Date remindStartDate = null;
			Date remindEndDate = null;
			if(map.get("remindstart")!=null) {
				Integer remindStart = Integer.parseInt(map.get("remindstart").toString());
				c.setTime(new Date()); 
				c.add(Calendar.DAY_OF_MONTH,remindStart);
				remindStartDate = c.getTime();
				map.put("remindstart", sdf.format(remindStartDate));
			}
			if(map.get("remindend")!=null) {
				Integer remindEnd = Integer.parseInt(map.get("remindend").toString());
				c.setTime(new Date());
				c.add(Calendar.DAY_OF_MONTH,remindEnd);
				remindEndDate = c.getTime();
				map.put("remindend", sdf.format(remindEndDate));
			}
		}
		
		Wrapper<DepartmentEntity> wrapper = new EntityWrapper<DepartmentEntity>();
		if(map.get("remindstart")!=null) {
			wrapper.ge(columnName, map.get("remindstart"));
		}
		if(map.get("remindend")!=null) {
			wrapper.le(columnName, map.get("remindend"));
		}


		int count = departmentService.selectCount(wrapper);
		return R.ok().put("count", count);
	}
	


}
