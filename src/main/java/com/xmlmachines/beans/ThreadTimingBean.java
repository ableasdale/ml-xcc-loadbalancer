package com.xmlmachines.beans;

public class ThreadTimingBean {

	private int atomicId;
	private String name;
	private long start;
	private long end;

	public int getAtomicId() {
		return atomicId;
	}

	public void setAtomicId(int atomicId) {
		this.atomicId = atomicId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getStart() {
		return start;
	}

	public void setStart(long start) {
		this.start = start;
	}

	public long getEnd() {
		return end;
	}

	public void setEnd(long end) {
		this.end = end;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + atomicId;
		result = prime * result + (int) (end ^ (end >>> 32));
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + (int) (start ^ (start >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ThreadTimingBean other = (ThreadTimingBean) obj;
		if (atomicId != other.atomicId)
			return false;
		if (end != other.end)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (start != other.start)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ThreadTimingBean [atomicId=" + atomicId + ", name=" + name
				+ ", start=" + start + ", end=" + end + "]";
	}

}
