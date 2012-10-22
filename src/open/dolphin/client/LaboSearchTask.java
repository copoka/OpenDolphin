/*
 * LaboTestSearchTask.java
 *
 * Created on 2003/01/27
 *
 * Last updated on 2003/02/28
 *
 */

package open.dolphin.client;

import java.util.*;

import open.dolphin.delegater.LaboDelegater;
import open.dolphin.dto.LaboSearchSpec;
import open.dolphin.infomodel.LaboItemValue;
import open.dolphin.infomodel.LaboModuleValue;
import open.dolphin.infomodel.LaboSpecimenValue;

/**
 * LaboSearchTask
 * 
 * @author  kazushi,Minagawa
 * 
 */
public class LaboSearchTask extends AbstractInfiniteTask {
    
    private LaboSearchSpec spec;
    private LaboDelegater ldl;
    
    private AllLaboTest allLaboTests;
    private Vector<SimpleLaboModule> moduleVec;

    /** Creates new LaboTestSearchTask */
    public LaboSearchTask(LaboSearchSpec spec, LaboDelegater ldl, int taskLength) {
        this.spec = spec;
        this.ldl = ldl;
        setTaskLength(taskLength);
    }
    
	public AllLaboTest getAllLaboTest() {
        return allLaboTests;
    }
    
	public Vector getLaboModuleColumns() {
        return moduleVec;
    }

    @SuppressWarnings("unchecked")
	protected void doTask() {
    	
    	// �f�[�^�x�[�X�������� LaboModuleValue �̃��X�g�𓾂�
        this.setMessage("�������Ă��܂�...");
        List<LaboModuleValue> results = (List<LaboModuleValue>) ldl.getLaboModules(spec);
        
        if (results == null || results.size() == 0) {
        	setMessage("�������I�����܂���");
        	setDone(true);
        	return;
        }
        
        // �������ʃe�[�u���̃J�����𐶐����邽�߂̃x�N�g��
        // ��J�������ꃂ�W���[���ɑΉ�����
        moduleVec = new Vector<SimpleLaboModule>();
        
        // LaboModuleValue���C�e���[�g���A�e�[�u���֕\���ł���f�[�^�ɕ�������
        for (LaboModuleValue moduleValue : results) {
        
        	// LaboModuleValu�̊ȈՔŃI�u�W�F�N�g�𐶐����x�N�g���ɉ�����
        	SimpleLaboModule simpleLaboModule = new SimpleLaboModule();
            moduleVec.add(simpleLaboModule);
       
            // �ȈՔłɒl��ݒ肷��
            simpleLaboModule.setSampleTime(moduleValue.getSampleTime());
            simpleLaboModule.setRegistTime(moduleValue.getRegistTime());
            simpleLaboModule.setReportTime(moduleValue.getReportTime());
            simpleLaboModule.setMmlConfirmDate(moduleValue.getConfirmDate());            
            simpleLaboModule.setReportStatus(moduleValue.getReportStatus());
            simpleLaboModule.setTestCenterName(moduleValue.getLaboratoryCenter());            
            simpleLaboModule.setSet(moduleValue.getSetName()); 
            
            // Module �Ɋ܂܂��W�{���C�e���[�g����
            Collection<LaboSpecimenValue> specimens = moduleValue.getLaboSpecimens();
            
            if (specimens != null) {
            	
            	for (LaboSpecimenValue bean : specimens) {
            		
            		// �ȈՔŃ��{�e�X�g�I�u�W�F�N�g�𐶐����ȈՔł̃��W���[���։�����
            		SimpleLaboTest laboTest = new SimpleLaboTest();
                    simpleLaboModule.addSimpleLaboTest(laboTest);
                    SimpleLaboSpecimen specimen = new SimpleLaboSpecimen();
                    laboTest.setSimpleSpecimen(specimen);
                    
                    specimen.setSpecimenCodeID(bean.getSpecimenCodeId());
                    specimen.setSpecimenCode(bean.getSpecimenCode());
                    specimen.setSpecimenName(bean.getSpecimenName());
                    
                    // �������ԂɊ܂܂��S�Ă̌�����ێ�����I�u�W�F�N�g - allLaboTests�𐶐�����
                    if (allLaboTests == null) {
                        allLaboTests = new AllLaboTest();
                    }
                    // �W�{���L�[�Ƃ��ēo�^����
                    allLaboTests.addSpecimen(specimen);
                    
                    // Specimen�Ɋ܂܂�� Item ���C�e���[�g����
                    Collection<LaboItemValue> items = bean.getLaboItems();
                    
                    if (items != null) {

                    	for (LaboItemValue itemBean : items) {
                    		
                    		// �������ڂ�W�{�L�[�̒l(TreeSet)�Ƃ��ēo�^����
                    		SimpleLaboTestItem testItem = new SimpleLaboTestItem();
                            LaboTestItemID testItemID = new LaboTestItemID();
                            
                            testItem.setItemCodeID(itemBean.getItemCodeId());
                            testItemID.setItemCodeID(itemBean.getItemCodeId());
                            
                            testItem.setItemCode(itemBean.getItemCode());
                            testItemID.setItemCode(itemBean.getItemCode());
                            
                            testItem.setItemName(trimJSpace(itemBean.getItemName()));
                            testItemID.setItemName(trimJSpace(itemBean.getItemName()));
                            
                            allLaboTests.addTestItem(specimen, testItemID);
                            
                            testItem.setItemValue(itemBean.getItemValue());
                            testItem.setItemUnit(itemBean.getUnit());
                            testItem.setLow(itemBean.getLow());
                            testItem.setUp(itemBean.getUp());
                            testItem.setNormal(itemBean.getNormal());
                            testItem.setOut(itemBean.getNout());
                            
                            laboTest.addSimpleLaboTestItem(testItem);
                    	}
                    }
            	}
            }
        }
        
        setMessage("�������I�����܂���");
        setDone(true);
    }
    
    private String trimJSpace(String str) {
        String ret = null;
        if (str != null) {
            int index = str.indexOf("�@");
            ret = index > 0 ? str.substring(0, index) : str;
        }
        return ret;
    }
}
