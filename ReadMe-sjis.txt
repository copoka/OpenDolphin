2013-04-16�@�F��a�j�@���C�t�T�C�G���X�R���s���[�e�B���O�i���j

�P�D���C�Z���X
�EOpenDolphin�̃��C�Z���X�� GNU GPL3 �ł��B
�EOpenDolphin�ɂ͎D�y�s������ӉȂ̏����搶�A�a�̎R�s���c���Ȃ̑��c�搶�̃R�[�h���܂܂�Ă��܂��B�����̕����̒��쌠�͂��ꂼ��̐搶�ɋA�����܂��B
�EOpenDolphin�̓��C�t�T�C�G���X�R���s���[�e�B���O�i���j�̓o�^���W�ł��B

�Q�D�J����
�Ejdk 1.7.0_17
�ENetBeans 7.3
�Emaven 3.0.3
�EJavaEE 6
�EJBoss-7.1.1.Final

�R�D�ˑ���
OpenDolphin�́Amaven �Ńv���W�F�N�g�Ǘ����s���Ă��܂��B
�r���h����ɂ�ext_lib����
�EiTextAsian.jar
�EAppleJavaExtensions.jar
�����[�J�����|�W�g���[�Ɏ蓮�ŃC���X�g�[������K�v������܂��B

mvn install:install-file -Dfile=/path/to/iTextAsian.jar -DgroupId=opendolphin -DartifactId=itext-font -Dversion=1.0 -Dpackaging=jar

mvn install:install-file -Dfile=/path/to/AppleJavaExtensions.jar
-DgroupId=com.apple -DartifactId=AppleJavaExtensions -Dversion=1.6
-Dpackaging=jar