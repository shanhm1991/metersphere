// 用例管理列表
export const GetCaseListUrl = '/functional/case/page';
// 用例管理-添加
export const CreateCaseUrl = '/functional/case/add';
// 用例管理-更新
export const UpdateCaseUrl = '/functional/case/update';
// 用例管理-删除
export const DeleteCaseUrl = '/functional/case/delete';
// 用例管理-详情
export const DetailCaseUrl = '/functional/case/detail';
// 用例管理-批量移动用例
export const BatchMoveCaseUrl = '/functional/case/batch/move';
// 用例管理-批量删除用例
export const BatchDeleteCaseUrl = '/functional/case/batch/delete-to-gc';
// 用例管理-批量删除用例
export const BatchEditCaseUrl = '/functional/case/batch/edit';
// 用例管理-批量复制
export const BatchCopyCaseUrl = '/functional/case/batch/copy';
// 用例管理-关注/取消关注用例
export const FollowerCaseUrl = '/functional/case/edit/follower';
// 获取用例关注人
export const GetCaseFollowerUrl = '/functional/case/follower';
// 获取用例模板自定义字段
export const GetCaseCustomFieldsUrl = '/functional/case/default/template/field';
// 获取表头自定义字段（高级搜索中的自定义字段）
export const GetSearchCustomFieldsUrl = '/functional/case/custom/field';
// 关联文件列表
export const GetAssociatedFilePageUrl = '/attachment/page';

// 获取模块树
export const GetCaseModuleTreeUrl = '/functional/case/module/tree';
// 创建模块树
export const CreateCaseModuleTreeUrl = '/functional/case/module/add';
// 更新模块树
export const UpdateCaseModuleTreeUrl = '/functional/case/module/update';
// 移动模块
export const MoveCaseModuleTreeUrl = '/functional/case/module/move';
// 回收站-模块-获取模块树
export const GetTrashCaseModuleTreeUrl = '/functional/case/module/trash/tree';
// 删除模块
export const DeleteCaseModuleTreeUrl = '/functional/case/module/delete';
// 获取默认模版自定义字段
export const GetDefaultTemplateFieldsUrl = '/functional/case/default/template/field';

// 回收站

// 回收站分页
export const GetRecycleCaseListUrl = '/functional/case/trash/page';
// 获取回收站模块数量
export const GetRecycleCaseModulesCountUrl = '/functional/case/trash/module/count';
// 获取全部用例模块数量
export const GetCaseModulesCountUrl = '/functional/case/module/count';
// 恢复回收站用例表
export const RestoreCaseListUrl = '/functional/case/trash/batch/recover';
// 批量彻底删除回收站用例表
export const BatchDeleteRecycleCaseListUrl = '/functional/case/trash/batch/delete';
// 恢复回收站单个用例
export const RecoverRecycleCaseListUrl = '/functional/case/trash/recover';
// 删除回收站单个用例
export const DeleteRecycleCaseListUrl = '/functional/case/trash/delete';

// 关联需求

// 已关联需求列表
export const GetDemandListUrl = '/functional/case/demand/page';
// 添加需求
export const AddDemandUrl = '/functional/case/demand/add';
// 更新需求
export const UpdateDemandUrl = '/functional/case/demand/update';
// 批量关联需求
export const BatchAssociationDemandUrl = '/functional/case/demand/batch/relevance';
// 取消关联
export const CancelAssociationDemandUrl = '/functional/case/demand/cancel';

// 附件管理
// 上传文件并关联用例
export const UploadOrAssociationFileUrl = '/attachment/upload/file';
// 转存文件
export const TransferFileUrl = '/attachment/transfer';
// 预览文件
export const PreviewFileUrl = '/attachment/preview';
// 下载文件
export const DownloadFileUrl = '/attachment/download';
// 删除文件或取消关联用例文件
export const deleteFileOrCancelAssociationUrl = '/attachment/delete/file';
// 获取转存目录
export const getTransferTreeUrl = '/attachment/options';
// 附件是否更新
export const GetFileIsUpdateUrl = '/attachment/update';
// 检查文件是否更新
export const checkFileIsUpdateUrl = '/attachment/check-update';

// 评论列表
export const GetCommentListUrl = '/functional/case/comment/get/list';
// 创建评论
export const CreateCommentItemUrl = '/functional/case/comment/save';
// 更新评论
export const UpdateCommentItemUrl = '/functional/case/comment/update';
// 删除评论
export const DeleteCommentItemUrl = '/functional/case/comment/delete';
// 获取详情用例评审
export const GetDetailCaseReviewUrl = '/functional/case/review/page';

export default {};