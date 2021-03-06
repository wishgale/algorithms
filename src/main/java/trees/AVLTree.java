package trees;

import java.util.ArrayList;
import java.util.List;

/**
 * AVLTree 平衡二叉搜索树
 * 提供了 Demo 级一般的实现. 主要研究该数据结构的优势, 时间复杂度和空间复杂度.
 * 最重要的平衡算法及对应的旋转算法, 查找, 插入, 删除.
 *
 * Created by Joseph at 2018/5/27 0027 19:23.
 */
public class AVLTree {

    private AVLNode root;

    /** tolerable difference height between left tree and right tree */
    private static final int MAX_DIFF_HEIGHT = 1;

    /** The element number of tree */
    private static int size = 0;

    public <T extends Comparable> void insert(T beInsert){
        if (null != beInsert){
            root = insert(beInsert, root);
            size++;
        }
    }

    public <T extends Comparable> void remove(T removed){
        if (
                null != removed &&
                null != root
           )
        {
            root = remove(removed, root);
            size--;
        }
    }

    public <T extends Comparable> boolean contains(T beSearch, AVLNode avlNode){
        boolean exists ;
        int result = beSearch.compareTo(avlNode.element);
        if ( result < 0 ){
            exists = contains(beSearch, avlNode.left);
        }
        else if ( result > 0 ){
            exists = contains(beSearch, avlNode.right);
        }
        else {
            exists = true;
        }
        return exists;
    }

    public <T extends Comparable> List<T> preorderTraversal(){
        if (root == null){
            return null;
        }
        return preorderTraversal(root, new ArrayList<>());
    }

    public <T extends Comparable> List<T> inorderTraversal(){
        if (root == null){
            return null;
        }
        return inorderTraversal(root, new ArrayList<>());
    }

    public <T extends Comparable> List<T> postorderTraversal(){
        if (root == null){
            return null;
        }
        return postorderTraversal(root, new ArrayList<>());
    }

    /* -------------------------------------------- 内部函数 Start -------------------------------------------------*/

    /**
     * Insert a T into the tree.
     * Return a sub tree node when every recursion.
     * The operation cost O(logN) time complexity.
     *
     * @param beInsert T
     * @param avlNode tree node
     * @param <T> T
     * @return node
     */
    private <T extends Comparable> AVLNode insert(T beInsert, AVLNode avlNode){
        if (null == avlNode){
            return new AVLNode(beInsert);
        }
        int result = beInsert.compareTo(avlNode.element);
        if (result < 0){
            avlNode.left = insert(beInsert, avlNode.left);
        }
        else if (result > 0){
            avlNode.right = insert(beInsert, avlNode.right);
        }
        else {
            // beInsert equals someone, we needn't it
        }

        // calculate height of node
        avlNode.height = Math.max(calculateHeight(avlNode.left), calculateHeight(avlNode.right)) + 1;

        // UnBalance maybe occur after insert, do balance fixing it
        return balance(avlNode);
    }

    /**
     * Remove specific T at the tree. Return a balanced sub tree.
     * The operation cost O(logN) time complexity.
     *
     * @param removed the element
     * @param avlNode node at Tree
     * @param <T> T
     * @return a balanced sub tree
     */
    private <T extends Comparable> AVLNode remove(T removed, AVLNode avlNode){
        if (null == avlNode){
            return null;
        }

        int result = removed.compareTo(avlNode.element);
        if ( result < 0 ){
            avlNode.left = remove(removed, avlNode.left);
        }
        else if ( result > 0 ){
            avlNode.right = remove(removed, avlNode.right);
        }
        else {
            // got the node. lets remove it

            if (null == avlNode.left && null == avlNode.right){
                /*
                    the node only have one reference that are parent left or right
                    this relationship will be cut off. the node will have collected by GC.
                 */
                avlNode = null;
            }
            // only have left
            else if (null == avlNode.right){
                avlNode = avlNode.left;
            }
            // only have right
            else if (null == avlNode.left){
                avlNode = avlNode.right;
            }
            // have left and right
            else {
                /*
                    we should find a node which greater than all left child
                    and less than all right child. that is most depth left of firstly right child.
                    After the removed was instead of minimum, we must remove the minimum.
                    A recursion process would be called which are same as before.
                 */
                AVLNode minimum = findMin(avlNode.right);
                avlNode.element = minimum.element;
                avlNode.right = remove(minimum.element, avlNode.right);
            }
        }

        // UnBalance maybe occur after remove, do balance fixing it
        return balance(avlNode);
    }

    /**
     * The algorithm for balance the tree.
     * Left Rotate and right rotate is two base operation whose combination can handle 4 unbalance case.
     *
     * @param avlNode a node be balance
     * @return a balanced sub tree
     */
    private AVLNode balance(AVLNode avlNode){
        if (null == avlNode){
            return avlNode;
        }
        // check if unbalance is occur
        int diff = Math.abs(calculateHeight(avlNode.left) - calculateHeight(avlNode.right));
        if (diff > MAX_DIFF_HEIGHT){
            // then we must fixing it, but we have to confirm which status of unbalance

            /*
                unbalance situation:
                    1, the new node has been inserted at left tree's left side (LL)
                    2, the new node has been inserted at left tree's right side (LR)
                    3, the new node has been inserted at right tree's left side (RL)
                    4, the new node has been inserted at right tree's right side (RR)
                we have 4 algorithm to handle corresponding situation.
                Mapping relationship:
                LL -> rightRotate
                RR -> leftRotate
                LR -> leftRotateThenRightRotate
                RL -> rightRotateThenLeftRotate
             */

            // firstly check is left tree or right tree
            int leftTreeHeight = calculateHeight(avlNode.left);
            int rightTreeHeight = calculateHeight(avlNode.right);

            int leftSideHeight ;
            int rightSideHeight;

            if (leftTreeHeight > rightTreeHeight){
                // confirm further left or right
                leftSideHeight = calculateHeight(avlNode.left.left);
                rightSideHeight = calculateHeight(avlNode.left.right);
                if (leftSideHeight > rightSideHeight){
                    // LL mode just do rightRotate can fixing it
                    avlNode = rightRotate(avlNode);
                }
                else {
                    // LR mode do leftRotateThenRightRotate
                    /*
                        1, firstly left rotate left node of avlNode.
                        2, secondly right rotate avlNode
                     */
                    avlNode.left = leftRotate(avlNode.left);
                    avlNode = rightRotate(avlNode);
                }
            }
            else {
                leftSideHeight = calculateHeight(avlNode.right.left);
                rightSideHeight = calculateHeight(avlNode.right.right);
                if (leftSideHeight > rightSideHeight){
                    // RL mode do rightRotateThenLeftRotate
                    avlNode.right = rightRotate(avlNode.right);
                    avlNode = leftRotate(avlNode);
                }
                else {
                    // RR mode do leftRotate
                    avlNode = leftRotate(avlNode);
                }
            }
        }

        /*
            Why calculate height of avlNode again even if it has been calculated in rotate algorithm ?
            Preventing the balance situation after insert or remove that needn't rotate.
            But the height of node was changed actually.
            So that need recalculate here.
         */
        avlNode.height = Math.max(calculateHeight(avlNode.left), calculateHeight(avlNode.right)) + 1;
        return avlNode;
    }

    private AVLNode rightRotate(AVLNode avlNode){
        AVLNode k1 = avlNode.left;
        avlNode.left = k1.right;
        k1.right = avlNode;

        /*
            two node height must recalculate
            The height of node depends on it's sub tree, it is nothing with it's parent node.
            K1 and avlNode are the node whose sub tree has been moved.
            we only need adjust them height.
         */
        avlNode.height = Math.max(calculateHeight(avlNode.left), calculateHeight(avlNode.right)) + 1;
        k1.height = Math.max(calculateHeight(k1.left), avlNode.height) + 1;

        return k1;
    }

    private AVLNode leftRotate(AVLNode avlNode){
        AVLNode k1 = avlNode.right;
        avlNode.right = k1.left;
        k1.left = avlNode;

        // two node height must recalculate
        avlNode.height = Math.max(calculateHeight(avlNode.left), calculateHeight(avlNode.right)) + 1;
        k1.height = Math.max(calculateHeight(k1.right), avlNode.height) + 1;

        return k1;
    }

    private AVLNode findMin(AVLNode avlNode){
        AVLNode minimum = null;
        if (null != avlNode){
            // if it have left child, the most depth left is minimum
            while (null != avlNode.left){
                avlNode = avlNode.left;
            }
            minimum = avlNode;
        }
        return minimum;
    }

    private AVLNode findMax(AVLNode avlNode){
        AVLNode maximum = null;
        if (null != avlNode){
            // the most depth right is maximum
            while (null != avlNode.right){
                avlNode = avlNode.right;
            }
            maximum = avlNode;
        }
        return maximum;
    }

    /** calculate the specific node's height */
    private int calculateHeight(AVLNode avlNode){
        if (null == avlNode){
            return -1;
        }
        return avlNode.height;
    }

    /**
     * preorder traversal
     *
     * @param root root
     * @param elementList elementList
     * @param <T> T
     * @return the list represent the tree's preorder traversal
     */
    private <T extends Comparable> List<T> preorderTraversal(AVLNode root, List<T> elementList){
        // base case
        if (null == root){
            return elementList;
        }
        // the order for preorder traversal is root left right
        elementList.add((T) root.element);
        preorderTraversal(root.left, elementList);
        preorderTraversal(root.right, elementList);
        return elementList;
    }

    /**
     * inorder traversal
     *
     * @param root root
     * @param elementList elementList
     * @param <T> T
     * @return the list represent the tree's inorder traversal
     */
    private <T extends Comparable> List<T> inorderTraversal(AVLNode root, List<T> elementList){
        // base case
        if (null == root){
            return elementList;
        }
        // the order for inorder traversal is left root right
        inorderTraversal(root.left, elementList);
        elementList.add((T) root.element);
        inorderTraversal(root.right, elementList);
        return elementList;
    }

    /**
     * postorder traversal
     *
     * @param root root
     * @param elementList elementList
     * @param <T> T
     * @return the list represent the tree's postorder traversal
     */
    private <T extends Comparable> List<T> postorderTraversal(AVLNode root, List<T> elementList){
        // base case
        if (null == root){
            return elementList;
        }
        // the order for postorder traversal is left right root
        postorderTraversal(root.left, elementList);
        postorderTraversal(root.right, elementList);
        elementList.add((T) root.element);
        return elementList;
    }

    /* -------------------------------------------- 内部函数 End -------------------------------------------------*/



    /** AVLTree Node */
    private static class AVLNode< T extends Comparable<? super T> > {
        int height;
        T element;
        AVLNode left;
        AVLNode right;

        AVLNode(T element){
            this (element, null, null);
        }

        AVLNode(T element, AVLNode left, AVLNode right){
            this.element = element;
            this.left = left;
            this.right = right;
            this.height = 0;
        }
    }
}
