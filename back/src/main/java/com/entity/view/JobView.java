package com.entity.view;

import com.entity.JobEntity;

import com.baomidou.mybatisplus.annotations.TableName;
import org.apache.commons.beanutils.BeanUtils;
import java.lang.reflect.InvocationTargetException;

import java.io.Serializable;
 

/**
 * 岗位
 * 后端返回视图实体辅助类   
 * （通常后端关联的表或者自定义的字段需要返回使用）
 * @author 
 * @email 
 * @date 2021-05-07 10:42:31
 */
@TableName("jobs")
public class JobView extends JobEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	public JobView(){
	}
 
 	public JobView(JobEntity JobEntity){
 	try {
			BeanUtils.copyProperties(this, JobEntity);
		} catch (IllegalAccessException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 		
	}
}
