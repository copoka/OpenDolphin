/*
 * NormalizedLaboTest.java
 *
 * Created on 2003/08/01, 9:26
 */

package open.dolphin.client;

import java.util.*;

/**
 * AllLaboTest
 * �����������ԂɊ܂܂��S�Ă̌�����ێ�����I�u�W�F�N�g�B
 * �W�{���L�[�ɂ��A���̕W�{�Ɋ܂܂�錟�����ڂ�TreeSet��l�Ƃ���B
 * ex.  ���� �L�[
 * �@�@�@��L�W�{(����)�Ɋւ���S�Ă̌������ڂ� TreeSet �ɕێ�����B
 * 
 * @author   Kazushi Minagawa, Digital Globe, Inc.
 */
public class AllLaboTest {
    
    private TreeMap<SimpleLaboSpecimen, TreeSet<LaboTestItemID>> allTests = new TreeMap<SimpleLaboSpecimen, TreeSet<LaboTestItemID>>();
    
    /** Creates a new instance of NormalizedLaboTest */
    public AllLaboTest() {
    }
    
    public void clear() {
        allTests.clear();
    }
    
    /**
     * �W�{��ǉ�����B
     * �W�{�����[�Ƃ��� TreeSet ���}�b�v�ɒǉ������B
     * @param specimen �W�{
     */
    @SuppressWarnings("unchecked")
	public void addSpecimen(SimpleLaboSpecimen specimen) {
        if (! allTests.containsKey(specimen)) {
            allTests.put(specimen, new TreeSet());
        }
    }
    
    /**
     * �W�{�Ɍ������ڂ�ǉ�����B
     * @param specimen �W�{
     * @param testItem ��������
     */
    @SuppressWarnings("unchecked")
	public void addTestItem(SimpleLaboSpecimen specimen, LaboTestItemID testItem) {
        
    	// �W�{�� TreeSet �𓾂�
        TreeSet treeSet = allTests.get(specimen);
        
        if (treeSet != null) {
            treeSet.add(testItem);
        }
    }
    
    public TreeMap getAllTests() {
        return allTests;
    }
    
    /**
     * �e�[�u���ɕ\������ꍇ�ɕK�v�ȍs����Ԃ��B
     * ����͕W�{�̐��Ɗe�W�{�Ɋ܂܂�錟�����ڂ̍��v�ƂȂ�B
     * @return �W�{�̐�+�e�W�{�Ɋ܂܂�錟�����ڂ̍��v
     */
    public int getRowCount() {
        
        int count = 0;
        
        Iterator iter = allTests.keySet().iterator();
        
        while (iter.hasNext()) {
            
            SimpleLaboSpecimen sp = (SimpleLaboSpecimen) iter.next();
            count++;
            
            Iterator it = allTests.get(sp).iterator();
            
            while(it.hasNext()) {
                it.next();
                count++;
            }
        }
        
        return count;
    }
    
    /**
     * �e�[�u���̃f�[�^�z��Ɍ����f�[�^����ݒ肷��B���o�����쐬���邽�߂̃��\�b�h�B
     * @param laboData �e�[�u���̃f�[�^�z��
     * @param startRow �l�̐ݒ���J�n����s
     * @param col �l��ݒ肷��J����
     */
    public void fillRow(Object[][] laboData, int startRow, int col) {
        
        Iterator iter = allTests.keySet().iterator();
        
        while (iter.hasNext()) {
            
            SimpleLaboSpecimen sp = (SimpleLaboSpecimen) iter.next();
            
            // �J�n�s�͕W�{���Ƃ���
            laboData[startRow++][col] = sp;
            
            // �ȍ~�̍s�̓e�X�g���ږ��Ƃ���
            Iterator it = allTests.get(sp).iterator();
            
            while(it.hasNext()) {
                
                LaboTestItemID id = (LaboTestItemID) it.next();
                
                laboData[startRow++][col] = id.getItemName();
            }
        }
    }
    
    public String toString() {
                
        StringBuilder buf = new StringBuilder();
        
        Iterator iter = allTests.keySet().iterator();
        
        while (iter.hasNext()) {
            
            SimpleLaboSpecimen sp = (SimpleLaboSpecimen) iter.next();
            
            buf.append("\n");
            buf.append(sp.getSpecimenName());
            buf.append("\n");
            
            Iterator it = allTests.get(sp).iterator();
            
            while (it.hasNext()) {
                
                LaboTestItemID id = (LaboTestItemID)it.next();
                
                buf.append(id.getItemName());
                buf.append("\n");
            }
        }
        
        return buf.toString();
    }
}
