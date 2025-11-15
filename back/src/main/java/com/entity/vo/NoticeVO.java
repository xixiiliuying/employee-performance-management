package com.entity.vo;

import com.entity.NoticeEntity;

import java.util.Date;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;


/**
 * 公告信息
 * 手机端接口返回实体辅助类 
 * （主要作用去除一些不必要的字段）
 * @author
 * @email
 * @date 2021-05-07 10:42:30
 */
public class NoticeVO implements Serializable {
	private static final long serialVersionUID = 1L;


	/**
	 * 公告标题
	 */
	private String title;

	/**
	 * 图片
	 */
	private String image;

	/**
	 * 内容
	 */
	private String content;

	/**
	 * 发布日期
	 */
	@JsonFormat(locale="zh", timezone="GMT+8", pattern="yyyy-MM-dd")
	@DateTimeFormat
	private Date publishDate;

	/**
	 * 发布人ID
	 */
	private Long publisherId;

	/**
	 * 创建时间
	 */
	@JsonFormat(locale="zh", timezone="GMT+8", pattern="yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat
	private Date createTime;


	/**
	 * 设置：公告标题
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * 获取：公告标题
	 */
	public String getTitle() {
		return title;
	}


	/**
	 * 设置：图片
	 */
	public void setImage(String image) {
		this.image = image;
	}

	/**
	 * 获取：图片
	 */
	public String getImage() {
		return image;
	}


	/**
	 * 设置：内容
	 */
	public void setContent(String content) {
		this.content = content;
	}

	/**
	 * 获取：内容
	 */
	public String getContent() {
		return content;
	}


	/**
	 * 设置：发布日期
	 */
	public void setPublishDate(Date publishDate) {
		this.publishDate = publishDate;
	}

	/**
	 * 获取：发布日期
	 */
	public Date getPublishDate() {
		return publishDate;
	}


	/**
	 * 设置：发布人ID
	 */
	public void setPublisherId(Long publisherId) {
		this.publisherId = publisherId;
	}

	/**
	 * 获取：发布人ID
	 */
	public Long getPublisherId() {
		return publisherId;
	}


	/**
	 * 设置：创建时间
	 */
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	/**
	 * 获取：创建时间
	 */
	public Date getCreateTime() {
		return createTime;
	}

}