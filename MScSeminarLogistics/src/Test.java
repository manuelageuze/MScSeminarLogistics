import java.util.ArrayList;
import java.util.List;

public class Test {

	public static void main(String[] args) {
		System.out.println("Test");
		int[] dim = {324, 860, 999};
		List<List<Integer>> perm = permute(dim);
		for (int i=0; i < perm.size(); i++) {
			for (int j=0; j < perm.get(i).size(); j++) {
				System.out.print(perm.get(i).get(j) + " ");
			}
			System.out.println();
		}
		
	}

	
	public static List<List<Integer>> permute(int[] nums) {
        List<List<Integer>> ans = new ArrayList<>();
        List<Integer> ds = new ArrayList<>();
        boolean[] freq = new boolean[nums.length];
        fun(nums, ds,ans, freq);
        return ans;
    }
    public static void fun(int[] nums,List<Integer> ds,List<List<Integer>> ans,boolean[] freq){
        if(ds.size()==nums.length){
            ans.add(new ArrayList<>(ds));
            return ;
        }
        for(int i=0;i<nums.length;i++){
            if(!freq[i]){
                freq[i] = true;
                ds.add(nums[i]);
                fun(nums, ds,ans, freq);
                ds.remove(ds.size()-1);
                freq[i] = false;
            }
        }
    }

}
