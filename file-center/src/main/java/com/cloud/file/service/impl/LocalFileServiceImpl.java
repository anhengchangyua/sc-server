package com.cloud.file.service.impl;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloud.file.dao.FileDao;
import com.cloud.file.model.FileInfo;
import com.cloud.file.model.FileSource;
import com.cloud.file.service.FileService;
import com.cloud.file.utils.FileUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * 本地存储文件<br>
 * 该实现文件服务只能部署一台<br>
 * 如多台机器间能共享到一个目录，即可部署多台
 * 
 * @author jh
 *
 */
@Slf4j
@Service("localFileServiceImpl")
public class LocalFileServiceImpl implements FileService {

	@Autowired
	private FileDao fileDao;
	@Value("${file.local.urlPrefix}")
	private String urlPrefix;
	/**
	 * 上传文件存储在本地的根路径
	 */
	@Value("${file.local.path}")
	private String localFilePath;

	@Override
	public FileInfo upload(MultipartFile file) throws Exception {
		FileInfo fileInfo = FileUtil.getFileInfo(file);
		FileInfo oldFileInfo = fileDao.getById(fileInfo.getId());
		if (oldFileInfo != null) {
			return oldFileInfo;
		}
		if (!fileInfo.getName().contains(".")) {
			throw new IllegalArgumentException("缺少后缀名");
		}

		int index = fileInfo.getName().lastIndexOf(".");
		// 文件扩展名
		String fileSuffix = fileInfo.getName().substring(index);

		String suffix = "/" + LocalDate.now().toString().replace("-", "/") + "/" + fileInfo.getId() + fileSuffix;

		String path = localFilePath + suffix;
		String url = urlPrefix + suffix;
		fileInfo.setPath(path);
		fileInfo.setUrl(url);

		FileUtil.saveFile(file, path);

		fileInfo.setSource(FileSource.LOCAL.name());
		fileDao.save(fileInfo);

		log.info("上传文件到本地：{}", fileInfo);

		return fileInfo;
	}

	@Override
	public void delete(FileInfo fileInfo) {
		FileUtil.deleteFile(fileInfo.getPath());
		fileDao.delete(fileInfo.getId());
		log.info("删除本地文件：{}", fileInfo);
	}
}
