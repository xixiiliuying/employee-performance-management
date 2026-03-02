package com.dao;

import com.entity.DepartmentEntity;
import com.baomidou.mybatisplus.mapper.BaseMapper;
import java.util.List;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;

import org.apache.ibatis.annotations.Param;
import com.entity.vo.DepartmentVO;
import com.entity.view.DepartmentView;


/**
 * 部门
 * 
 * @author 
 * @email 
 * @date 2021-05-07 10:42:31
 */
public interface DepartmentDao extends BaseMapper<DepartmentEntity> {
	
	List<DepartmentVO> selectListVO(@Param("ew") Wrapper<DepartmentEntity> wrapper);
	
	DepartmentVO selectVO(@Param("ew") Wrapper<DepartmentEntity> wrapper);
	
	List<DepartmentView> selectListView(@Param("ew") Wrapper<DepartmentEntity> wrapper);

	List<DepartmentView> selectListView(Pagination page, @Param("ew") Wrapper<DepartmentEntity> wrapper);
	
	DepartmentView selectView(@Param("ew") Wrapper<DepartmentEntity> wrapper);

	int deleteBatch(@Param("idList") List<Long> idList);
	
}
