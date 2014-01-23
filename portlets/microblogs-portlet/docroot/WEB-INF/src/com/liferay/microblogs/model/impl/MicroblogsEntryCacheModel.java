/**
 * Copyright (c) 2000-2013 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.microblogs.model.impl;

import com.liferay.microblogs.model.MicroblogsEntry;

import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.model.CacheModel;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import java.util.Date;

/**
 * The cache model class for representing MicroblogsEntry in entity cache.
 *
 * @author Brian Wing Shun Chan
 * @see MicroblogsEntry
 * @generated
 */
public class MicroblogsEntryCacheModel implements CacheModel<MicroblogsEntry>,
	Externalizable {
	@Override
	public String toString() {
		StringBundler sb = new StringBundler(23);

		sb.append("{microblogsEntryId=");
		sb.append(microblogsEntryId);
		sb.append(", companyId=");
		sb.append(companyId);
		sb.append(", userId=");
		sb.append(userId);
		sb.append(", userName=");
		sb.append(userName);
		sb.append(", createDate=");
		sb.append(createDate);
		sb.append(", modifiedDate=");
		sb.append(modifiedDate);
		sb.append(", content=");
		sb.append(content);
		sb.append(", type=");
		sb.append(type);
		sb.append(", receiverUserId=");
		sb.append(receiverUserId);
		sb.append(", receiverMicroblogsEntryId=");
		sb.append(receiverMicroblogsEntryId);
		sb.append(", socialRelationType=");
		sb.append(socialRelationType);
		sb.append("}");

		return sb.toString();
	}

	@Override
	public MicroblogsEntry toEntityModel() {
		MicroblogsEntryImpl microblogsEntryImpl = new MicroblogsEntryImpl();

		microblogsEntryImpl.setMicroblogsEntryId(microblogsEntryId);
		microblogsEntryImpl.setCompanyId(companyId);
		microblogsEntryImpl.setUserId(userId);

		if (userName == null) {
			microblogsEntryImpl.setUserName(StringPool.BLANK);
		}
		else {
			microblogsEntryImpl.setUserName(userName);
		}

		if (createDate == Long.MIN_VALUE) {
			microblogsEntryImpl.setCreateDate(null);
		}
		else {
			microblogsEntryImpl.setCreateDate(new Date(createDate));
		}

		if (modifiedDate == Long.MIN_VALUE) {
			microblogsEntryImpl.setModifiedDate(null);
		}
		else {
			microblogsEntryImpl.setModifiedDate(new Date(modifiedDate));
		}

		if (content == null) {
			microblogsEntryImpl.setContent(StringPool.BLANK);
		}
		else {
			microblogsEntryImpl.setContent(content);
		}

		microblogsEntryImpl.setType(type);
		microblogsEntryImpl.setReceiverUserId(receiverUserId);
		microblogsEntryImpl.setReceiverMicroblogsEntryId(receiverMicroblogsEntryId);
		microblogsEntryImpl.setSocialRelationType(socialRelationType);

		microblogsEntryImpl.resetOriginalValues();

		return microblogsEntryImpl;
	}

	@Override
	public void readExternal(ObjectInput objectInput) throws IOException {
		microblogsEntryId = objectInput.readLong();
		companyId = objectInput.readLong();
		userId = objectInput.readLong();
		userName = objectInput.readUTF();
		createDate = objectInput.readLong();
		modifiedDate = objectInput.readLong();
		content = objectInput.readUTF();
		type = objectInput.readInt();
		receiverUserId = objectInput.readLong();
		receiverMicroblogsEntryId = objectInput.readLong();
		socialRelationType = objectInput.readInt();
	}

	@Override
	public void writeExternal(ObjectOutput objectOutput)
		throws IOException {
		objectOutput.writeLong(microblogsEntryId);
		objectOutput.writeLong(companyId);
		objectOutput.writeLong(userId);

		if (userName == null) {
			objectOutput.writeUTF(StringPool.BLANK);
		}
		else {
			objectOutput.writeUTF(userName);
		}

		objectOutput.writeLong(createDate);
		objectOutput.writeLong(modifiedDate);

		if (content == null) {
			objectOutput.writeUTF(StringPool.BLANK);
		}
		else {
			objectOutput.writeUTF(content);
		}

		objectOutput.writeInt(type);
		objectOutput.writeLong(receiverUserId);
		objectOutput.writeLong(receiverMicroblogsEntryId);
		objectOutput.writeInt(socialRelationType);
	}

	public long microblogsEntryId;
	public long companyId;
	public long userId;
	public String userName;
	public long createDate;
	public long modifiedDate;
	public String content;
	public int type;
	public long receiverUserId;
	public long receiverMicroblogsEntryId;
	public int socialRelationType;
}