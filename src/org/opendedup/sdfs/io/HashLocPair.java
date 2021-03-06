package org.opendedup.sdfs.io;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.opendedup.hashing.HashFunctionPool;
import org.opendedup.rabin.utils.StringUtils;

import com.google.common.collect.Range;

public class HashLocPair implements Comparable<HashLocPair>, Externalizable {
	public static final int BAL = HashFunctionPool.hashLength + 8 + 4 + 4 + 4
			+ 4;
	public byte[] hash;
	public byte[] hashloc;
	public byte[] data;
	public int len;
	public int pos;
	public int offset;
	public int nlen;

	public byte[] asArray() throws IOException {
		ByteBuffer bf = ByteBuffer.wrap(new byte[BAL]);
		bf.put(hash);
		bf.put(hashloc);
		bf.putInt(len);
		bf.putInt(pos);
		bf.putInt(offset);
		bf.putInt(nlen);
		this.checkCorrupt();
		return bf.array();
	}

	private void checkCorrupt() throws IOException {
		if (len < 0 || pos < 0 || offset < 0 || nlen < 0)
			throw new IOException("data is corrupt " + this);
	}

	public boolean isInvalid() {
		return (len <= 0 || pos < 0 || offset < 0 || nlen <= 0);
	}

	public HashLocPair() {

	}

	private int currentPos = 1;

	public synchronized void addHashLoc(byte loc) {
		// SDFSLogger.getLog().info("set " + this.currentPos + " to " + loc);
		if (currentPos < this.hashloc.length) {
			if (this.hashloc[0] == -1)
				this.hashloc[0] = 0;
			this.hashloc[currentPos] = loc;
			this.currentPos++;
		}
	}

	public void resetHashLoc() {
		this.hashloc = new byte[8];
		this.hashloc[0] = -1;
	}

	public HashLocPair(byte[] b) throws IOException {
		ByteBuffer bf = ByteBuffer.wrap(b);
		hash = new byte[HashFunctionPool.hashLength];
		hashloc = new byte[8];
		bf.get(hash);
		bf.get(hashloc);
		len = bf.getInt();
		pos = bf.getInt();
		offset = bf.getInt();
		nlen = bf.getInt();
		this.checkCorrupt();
	}

	@Override
	public int compareTo(HashLocPair p) {
		if (this.pos == p.pos)
			return 0;
		if (this.pos > p.pos)
			return 1;
		else
			return -1;
	}

	public HashLocPair clone() {
		HashLocPair p = new HashLocPair();
		p.hash = Arrays.copyOf(this.hash, this.hash.length);
		p.hashloc = Arrays.copyOf(this.hashloc, this.hashloc.length);
		p.len = len;
		p.pos = pos;
		p.offset = offset;
		p.nlen = nlen;
		return p;
	}

	public Range<Integer> getRange() {
		return Range.closed(pos, pos + nlen);
	}

	public String toString() {
		String hashlocs = "[";
		for (byte b : this.hashloc) {
			hashlocs = hashlocs + Byte.toString(b) + " ";
		}
		hashlocs = hashlocs + "]";
		return "pos=" + pos + " len=" + len + " offset=" + offset + " nlen="
				+ nlen + " ep=" + (pos + nlen) + " hash="
				+ StringUtils.getHexString(hash) + " hashlocs=" + hashlocs;
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		in.readInt();
		this.hash = new byte[in.readInt()];
		in.read(this.hash);
		this.hashloc = new byte[8];
		in.read(this.hashloc);
		
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		ByteBuffer bf = ByteBuffer.wrap(new byte[4+this.hash.length+this.hashloc.length]);
		bf.putInt(this.hash.length);
		bf.put(hash);
		bf.put(hashloc);
		byte[] b = bf.array();
		out.writeInt(b.length);
		out.write(b);
		
	}

}