import java.util.List;


public class Pattern {
	
	private List<Cell> cellsInPattern;
	
	//初始化一个新的Pattern实例，存储了一个Cell对象的列表。
	public Pattern (List<Cell> elts) {
		this.cellsInPattern = elts;
	}
	
	//比较当前模式与另一个模式是否相同。这要求两个模式包含完全相同数量的单元格，且相应位置的单元格严格相等。
	public boolean isEqual(Pattern p2) {
		if (this.cellsInPattern.size() != p2.cellsInPattern.size())
			return false;
		for (int i = 0; i < this.cellsInPattern.size(); i++) {
			if (this.cellsInPattern.get(i) != p2.cellsInPattern.get(i))
				return false;
		}
		return true;
	}
	
	//重写Object类的equals方法，提高其适用性，用于判断两个Pattern实例是否等同。主要用于确保可以在集合中正确比较模式对象。
	@Override 
	public boolean equals (Object other){
	    if (other == null) return false;
	    if (other == this) return true;
	    if (!(other instanceof Pattern))return false;
	    Pattern otherMyClass = (Pattern)other;
	    if (this.isEqual(otherMyClass))
	    	return true;
	    else
	    	return false;
	}
	
	//重写Object类的hashCode方法，为Pattern提供一致的哈希码计算逻辑，这符合equals方法的合同。通常在将Pattern用作哈希表的键时需要。
	@Override
    public int hashCode() {
		final int prime = 31;
		int result = 1;
		for( Cell s : cellsInPattern ){
		    result = result * prime + s.hashCode();
		}
		return result;
    }
	
	//重写Object类的toString方法，返回模式的字符串表示，显示了组成模式的所有单元格的名称。
	@Override
	public String toString() {
		String tbr = "[";
		for (Cell c: cellsInPattern) {
			tbr = tbr + "(" + c.getName() + "),";
		}
		tbr = tbr.substring(0, tbr.length()-1);
		tbr = tbr + "]";
		return tbr;
	}

}
