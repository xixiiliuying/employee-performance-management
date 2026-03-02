package com.dao;

import com.entity.JobEntity;
import com.baomidou.mybatisplus.mapper.BaseMapper;
import java.util.List;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;

import org.apache.ibatis.annotations.Param;
import com.entity.vo.JobVO;
import com.entity.view.JobView;


/**
 * 岗位
 * 
 * @author 
 * @email 
 * @date 2021-05-07 10:42:31
 */
public interface JobDao extends BaseMapper<JobEntity> {
	
	List<JobVO> selectListVO(@Param("ew") Wrapper<JobEntity> wrapper);
	
	JobVO selectVO(@Param("ew") Wrapper<JobEntity> wrapper);
	
	List<JobView> selectListView(@Param("ew") Wrapper<JobEntity> wrapper);

	List<JobView> selectListView(Pagination page, @Param("ew") Wrapper<JobEntity> wrapper);
	
	JobView selectView(@Param("ew") Wrapper<JobEntity> wrapper);
	
}
