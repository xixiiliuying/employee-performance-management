package com.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 公告信息
 * 数据库通用操作实体类（普通增删改查）
 * @author
 * @email
 * @date 2021-05-07 10:42:30
 */
@TableName("notices")
public class NoticeEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 主键id (AUTO_INCREMENT)
	 */
	@TableId(value = "id", type = IdType.AUTO)
	private Long id;

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
	@JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date publishDate;

	/**
	 * 发布人ID
	 */
	private Long publisherId;

	/**
	 * 创建时间
	 */
	@JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;

	/**
	 * 更新时间
	 */
	@JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date updateTime;

	// ==================== Getter and Setter ====================

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Date getPublishDate() {
		return publishDate;
	}

	public void setPublishDate(Date publishDate) {
		this.publishDate = publishDate;
	}

	public Long getPublisherId() {
		return publisherId;
	}

	public void setPublisherId(Long publisherId) {
		this.publisherId = publisherId;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	// 构造函数
	public NoticeEntity() {
	}

	public NoticeEntity(Long id, String title, String image, String content, Date publishDate, Long publisherId, Date createTime, Date updateTime) {
		this.id = id;
		this.title = title;
		this.image = image;
		this.content = content;
		this.publishDate = publishDate;
		this.publisherId = publisherId;
		this.createTime = createTime;
		this.updateTime = updateTime;
	}
}
