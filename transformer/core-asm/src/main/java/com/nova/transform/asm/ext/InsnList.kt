package com.nova.transform.asm.ext

import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.InsnList

fun <T> Iterator<T>.asIterable(): Iterable<T> = Iterable { this }

fun InsnList.asIterable() = this.iterator().asIterable()

// ------------------------------------------------------------------------
//  Find the first instruction node with the specified opcode
// ------------------------------------------------------------------------
fun InsnList.find(opcode: Int) = this.asIterable().find { it.opcode == opcode }

// ------------------------------------------------------------------------
// Find all of instruction nodes with the specified opcode
// ------------------------------------------------------------------------
fun InsnList.findAll(vararg opcodes: Int) = this.filter { it.opcode in opcodes }

fun InsnList.filter(predicate: (AbstractInsnNode) -> Boolean) = this.asIterable().filter(predicate)

fun InsnList.any(predicate: (AbstractInsnNode) -> Boolean) = this.asIterable().any(predicate)
