package dev.kaiwen.common.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import org.springframework.data.domain.Page;

/**
 * 分页查询结果封装。
 *
 * @param <T> 当前页数据元素类型
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
public class PageDto<T> {

  /** 当前页数据 */
  private final List<T> content;
  /** 总记录数 */
  private final long total;
  /** 当前页码（从 0 开始） */
  private final int page;
  /** 每页大小 */
  private final int size;
  /** 总页数 */
  private final int totalPages;

  @JsonCreator
  private PageDto(
      @JsonProperty("content") List<T> content,
      @JsonProperty("total") long total,
      @JsonProperty("page") int page,
      @JsonProperty("size") int size,
      @JsonProperty("totalPages") Integer ignoredTotalPages) {
    if (total < 0) {
      throw new IllegalArgumentException("total must be >= 0");
    }
    if (page < 0) {
      throw new IllegalArgumentException("page must be >= 0");
    }
    if (size <= 0) {
      throw new IllegalArgumentException("size must be greater than 0");
    }

    this.content = content == null ? Collections.emptyList() : content;
    this.total = total;
    this.page = page;
    this.size = size;
    this.totalPages = (int) Math.ceil((double) total / size);
  }

  /**
   * 构建分页结果。
   *
   * @param content 当前页数据
   * @param total 总记录数
   * @param page 当前页码（从 0 开始）
   * @param size 每页大小
   */
  public static <T> PageDto<T> of(List<T> content, long total, int page, int size) {
    return new PageDto<>(content, total, page, size, null);
  }

  /**
   * 从 Spring Data 的 {@link Page} 直接转换。
   *
   * @param page Spring Data 分页结果
   * @return 封装后的 PageDto
   * @throws IllegalArgumentException 当 page 为 null，或 page 的 size 为 0 时
   */
  public static <T> PageDto<T> from(Page<T> page) {
    if (page == null) {
      throw new IllegalArgumentException("page must not be null");
    }
    return new PageDto<>(
        page.getContent(),
        page.getTotalElements(),
        page.getNumber(),
        page.getSize(),
        null);
  }

  /** 是否有下一页 */
  public boolean hasNext() {
    return page + 1 < totalPages;
  }

  /** 是否有上一页 */
  public boolean hasPrevious() {
    return page > 0;
  }

  /** 是否为首页 */
  @JsonIgnore
  public boolean isFirst() {
    return !hasPrevious();
  }

  /** 是否为末页 */
  @JsonIgnore
  public boolean isLast() {
    return !hasNext();
  }

  /** 是否为空（无数据） */
  @JsonIgnore
  public boolean isEmpty() {
    return content.isEmpty();
  }
}
