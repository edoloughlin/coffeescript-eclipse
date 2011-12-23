package csep.tests

import org.junit.Test

class AssignTest extends ParserTestBase { 

  @Test
  def void testBasic() {
    ok('a = 3')
    ok('''
      a = 3
      b = 4
      ''')
  }

  /**
   * Unfortunately, checking is not called by the testing framework,
   * so this one will succeed no matter what.
   */
  @Test
  def void testReassign() {
    ok('''
      a = 2
      a = 3
    ''')	
  }
  
  @Test
  def void testDestructure() {
    ok('[a, b] = [1, 2]')	
  }
  
  @Test
  def void testBoolean() {
    ok('a or= 2')
    ok('a ||= 1')
    ok('a ?= b')
  }

  @Test
  def void testCompound() {
    ok('a = b = 1')
    ok('a = ++b')
    ok('[a, b, c] = [1, 2, 3]')
  }
  
  @Test
  def void testUnassignable() {
  	error('1 = 2')
  	error('(a + b) = 2')
  	ok('foo.bar = 3')
  	error('foo.bar() = 3')
  }
  
  @Test
  def void testThisProperty() {
  	ok('@count = 1')
  	ok('@server.production.name = "local"')  	
  	ok('this.owner.age = 32')
  }
  
  @Test
  def void testProperty() {
  	ok('a.b = 1')
  	ok('Clazz::counter = 1')
  }
}
