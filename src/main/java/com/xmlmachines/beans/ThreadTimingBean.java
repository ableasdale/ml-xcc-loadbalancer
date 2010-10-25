package com.xmlmachines.beans;

// TODO: Auto-generated Javadoc
/**
 * The Class ThreadTimingBean.
 */
public class ThreadTimingBean {

	/** The atomic id. */
	private int atomicId;

	/** The name. */
	private String name;

	/** The start. */
	private long start;

	/** The end. */
	private long end;

	/**
	 * Gets the atomic id.
	 * 
	 * @return the atomic id
	 */
	public int getAtomicId() {
		return atomicId;
	}

	/**
	 * Sets the atomic id.
	 * 
	 * @param atomicId
	 *            the new atomic id
	 */
	public void setAtomicId(int atomicId) {
		this.atomicId = atomicId;
	}

	/**
	 * Gets the name.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name.
	 * 
	 * @param name
	 *            the new name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the start.
	 * 
	 * @return the start
	 */
	public long getStart() {
		return start;
	}

	/**
	 * Sets the start.
	 * 
	 * @param start
	 *            the new start
	 */
	public void setStart(long start) {
		this.start = start;
	}

	/**
	 * Gets the end.
	 * 
	 * @return the end
	 */
	public long getEnd() {
		return end;
	}

	/**
	 * Sets the end.
	 * 
	 * @param end
	 *            the new end
	 */
	public void setEnd(long end) {
		this.end = end;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ThreadTimingBean [atomicId=" + atomicId + ", name=" + name
				+ ", start=" + start + ", end=" + end + "]";
	}

}
