package com.service;

import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.service.IService;
import com.utils.PageUtils;
import com.entity.JobEntity;
import java.util.List;
import java.util.Map;
import com.entity.vo.JobVO;
import org.apache.ibatis.annotations.Param;
import com.entity.view.JobView;


/**
 * 岗位
 *
 * @author 
 * @email 
 * @date 2021-05-07 10:42:31
 */
public interface JobService extends IService<JobEntity> {

    PageUtils queryPage(Map<String, Object> params);
    
   	List<JobVO> selectListVO(Wrapper<JobEntity> wrapper);
   	
   	JobVO selectVO(@Param("ew") Wrapper<JobEntity> wrapper);
   	
   	List<JobView> selectListView(Wrapper<JobEntity> wrapper);
   	
   	JobView selectView(@Param("ew") Wrapper<JobEntity> wrapper);
   	
   	PageUtils queryPage(Map<String, Object> params,Wrapper<JobEntity> wrapper);
   	
}

