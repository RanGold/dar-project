package lir;

public class NodeLirTrans {
	public String codeTrans;
	public String result;
	public ResultType type;
	
	public NodeLirTrans(String codeTrans){
		this.codeTrans = codeTrans;
		this.type = ResultType.Empty;
	}
	
	public NodeLirTrans(String codeTrans, String result){
		this.codeTrans = codeTrans;
		this.result = result;
		this.type = ResultType.Register;
	}
	
	public NodeLirTrans(String codeTrans, String result, ResultType type){
		this.codeTrans = codeTrans;
		this.result = result;
		this.type = type;
	}
}
