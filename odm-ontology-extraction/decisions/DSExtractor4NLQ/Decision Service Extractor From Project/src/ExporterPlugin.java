import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.EList;

import com.ibm.rules.decisionservice.model.IDsOperationVariable;
import com.ibm.rules.studio.model.decisionservice.IOperation;
import com.ibm.rules.studio.model.decisionservice.IOperationFolder;

import ilog.rules.studio.model.IlrRuleModel;
import ilog.rules.studio.model.IlrStudioModelPlugin;
import ilog.rules.studio.model.base.IlrModelFolder;
import ilog.rules.studio.model.base.IlrRuleProject;
import ilog.rules.studio.model.util.IlrBrmUtil;

// Just a start of an implementation to explore the signature of the Decision Service operation
// Not yet doing the job

public class ExporterPlugin {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("ExporterPlugin");
		
		IlrStudioModelPlugin plugin = IlrStudioModelPlugin.getPluginInstance();
		if (plugin == null)  {
			System.out.println("plugin is null");
			System.exit(-1);
		}
		
		IlrRuleModel ruleModel = IlrStudioModelPlugin.getRuleModel();
		if (ruleModel == null)  {
			System.out.println("RuleModel is null");
			System.exit(-1);
		}
		
		EList<IlrRuleProject> ruleModels = IlrStudioModelPlugin.getRuleModel().getRuleProjects();
		Iterator<IlrRuleProject> ruleProjectIterator = ruleModels.iterator();
		while(ruleProjectIterator.hasNext())  {
			System.out.println("project: " + ruleProjectIterator.next().getName());
		}
		
		IlrRuleProject ruleProject = IlrStudioModelPlugin.getRuleModel().getRuleProject("LoanValidationService");
		try {
			findOperations(ruleProject);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	public static void findOperations(IlrRuleProject p) throws CoreException {
        IlrModelFolder deploymentFolder = p.getModelFolders(IOperationFolder.ID)[0];
        
        for (Object e : IlrBrmUtil.getModelFolderContents(deploymentFolder)) {
        	System.out.printf("Object %s%n", e);
        	
        	if (e instanceof IOperation) {
                IOperation op = (IOperation) e;

                System.out.printf("Operation %s%n", op.getName());
                for (IDsOperationVariable v : op.getReferencedVariableList()) {
                    System.out.printf("  %s %s%n", v.getDsDirection(), v.getVariable());
                }
            }
        }
    }
}
